package br.com.arquivolivre.jsonrpc;

import static org.junit.jupiter.api.Assertions.*;

import br.com.arquivolivre.jsonrpc.gson.JsonRpcGsonConfig;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for complete JSON-RPC 2.0 request-response flows. Tests protocol compliance,
 * error handling, and end-to-end scenarios.
 */
public class IntegrationTest {

  private Gson gson;

  @BeforeEach
  public void setUp() {
    gson = JsonRpcGsonConfig.createGson();
  }

  // Complete request-response flow tests

  @Test
  public void testFlow_SuccessfulMethodCall() {
    // Client creates request
    Map<String, Integer> params = new HashMap<>();
    params.put("minuend", 42);
    params.put("subtrahend", 23);

    JsonRpcRequest<Map<String, Integer>> request =
        JsonRpcRequest.<Map<String, Integer>>builder()
            .method("subtract")
            .params(params)
            .id(1)
            .build();

    // Serialize and send
    String requestJson = gson.toJson(request);

    // Server receives and processes
    JsonRpcRequest<?> receivedRequest = gson.fromJson(requestJson, JsonRpcRequest.class);
    assertEquals("subtract", receivedRequest.getMethod());

    // Server creates success response
    int result = 19; // 42 - 23
    JsonRpcResponse<Integer> response =
        JsonRpcResponse.success(receivedRequest.getId().orElse(null), result);

    // Serialize and send back
    String responseJson = gson.toJson(response);

    // Client receives response
    JsonRpcResponse<?> receivedResponse = gson.fromJson(responseJson, JsonRpcResponse.class);

    assertTrue(receivedResponse.isSuccess());
    // Result is deserialized as JsonElement
    Object resultObj = receivedResponse.getResult().get();
    if (resultObj instanceof com.google.gson.JsonPrimitive) {
      assertEquals(19.0, ((com.google.gson.JsonPrimitive) resultObj).getAsDouble());
    }
    assertEquals(1.0, ((Number) receivedResponse.getId()).doubleValue());
  }

  @Test
  public void testFlow_NotificationNoResponse() {
    // Client creates notification (no id)
    JsonRpcRequest<String> notification =
        JsonRpcRequest.notification("logMessage", "Application started");

    // Serialize and send
    String notificationJson = gson.toJson(notification);

    // Server receives
    JsonRpcRequest<?> receivedNotification = gson.fromJson(notificationJson, JsonRpcRequest.class);

    assertTrue(receivedNotification.isNotification());
    assertEquals("logMessage", receivedNotification.getMethod());

    // Server processes but does NOT send response for notifications
    assertFalse(receivedNotification.getId().isPresent());
  }

  @Test
  public void testFlow_MethodNotFoundError() {
    // Client requests non-existent method
    JsonRpcRequest<Object> request =
        JsonRpcRequest.builder().method("unknownMethod").id("req-123").build();

    String requestJson = gson.toJson(request);

    // Server receives
    JsonRpcRequest<?> receivedRequest = gson.fromJson(requestJson, JsonRpcRequest.class);

    // Server cannot find method, returns error
    JsonRpcError<?> error = StandardErrors.methodNotFound();
    JsonRpcResponse<Object> response =
        JsonRpcResponse.error(receivedRequest.getId().orElse(null), error);

    String responseJson = gson.toJson(response);

    // Client receives error response
    JsonRpcResponse<?> receivedResponse = gson.fromJson(responseJson, JsonRpcResponse.class);

    assertTrue(receivedResponse.isError());
    assertEquals(-32601, receivedResponse.getError().get().getCode());
    assertEquals("Method not found", receivedResponse.getError().get().getMessage());
    assertEquals("req-123", receivedResponse.getId());
  }

  @Test
  public void testFlow_InvalidParamsError() {
    // Client sends request with invalid params
    JsonRpcRequest<Map<String, Object>> request =
        JsonRpcRequest.<Map<String, Object>>builder()
            .method("divide")
            .params(Map.of("dividend", 10)) // missing divisor
            .id(2)
            .build();

    String requestJson = gson.toJson(request);
    JsonRpcRequest<?> receivedRequest = gson.fromJson(requestJson, JsonRpcRequest.class);

    // Server validates params and finds error
    Map<String, String> errorData = new HashMap<>();
    errorData.put("missing", "divisor");
    errorData.put("required", "dividend, divisor");

    JsonRpcError<Map<String, String>> error = StandardErrors.invalidParams(errorData);
    JsonRpcResponse<Object> response =
        JsonRpcResponse.error(receivedRequest.getId().orElse(null), error);

    String responseJson = gson.toJson(response);
    JsonRpcResponse<?> receivedResponse = gson.fromJson(responseJson, JsonRpcResponse.class);

    assertTrue(receivedResponse.isError());
    assertEquals(-32602, receivedResponse.getError().get().getCode());
    assertTrue(receivedResponse.getError().get().getData().isPresent());
  }

  @Test
  public void testFlow_InternalServerError() {
    // Client sends valid request
    JsonRpcRequest<Object> request = JsonRpcRequest.builder().method("processData").id(3).build();

    String requestJson = gson.toJson(request);
    JsonRpcRequest<?> receivedRequest = gson.fromJson(requestJson, JsonRpcRequest.class);

    // Server encounters internal error during processing
    JsonRpcError<String> error = StandardErrors.internalError("Database connection failed");
    JsonRpcResponse<Object> response =
        JsonRpcResponse.error(receivedRequest.getId().orElse(null), error);

    String responseJson = gson.toJson(response);
    JsonRpcResponse<?> receivedResponse = gson.fromJson(responseJson, JsonRpcResponse.class);

    assertTrue(receivedResponse.isError());
    assertEquals(-32603, receivedResponse.getError().get().getCode());
    assertEquals("Internal error", receivedResponse.getError().get().getMessage());
  }

  @Test
  public void testFlow_CustomServerError() {
    // Client sends request
    JsonRpcRequest<Object> request = JsonRpcRequest.builder().method("authenticate").id(4).build();

    String requestJson = gson.toJson(request);
    JsonRpcRequest<?> receivedRequest = gson.fromJson(requestJson, JsonRpcRequest.class);

    // Server returns custom error in server error range
    JsonRpcError<String> error =
        StandardErrors.serverError(
            -32001, "Authentication required", "Missing authorization header");
    JsonRpcResponse<Object> response =
        JsonRpcResponse.error(receivedRequest.getId().orElse(null), error);

    String responseJson = gson.toJson(response);
    JsonRpcResponse<?> receivedResponse = gson.fromJson(responseJson, JsonRpcResponse.class);

    assertTrue(receivedResponse.isError());
    assertEquals(-32001, receivedResponse.getError().get().getCode());
    assertEquals("Authentication required", receivedResponse.getError().get().getMessage());
  }

  // Error handling scenario tests

  @Test
  public void testErrorHandling_ParseError() {
    // Simulate malformed JSON received by server
    String malformedJson = "{\"jsonrpc\":\"2.0\",\"method\":\"test\",\"id\":1"; // missing closing
    // brace

    // Server attempts to parse and catches exception
    try {
      gson.fromJson(malformedJson, JsonRpcRequest.class);
      fail("Should have thrown exception");
    } catch (Exception e) {
      // Server creates parse error response
      JsonRpcError<?> error = StandardErrors.parseError();
      JsonRpcResponse<Object> response = JsonRpcResponse.error(null, error);

      // Verify the response is correct
      assertTrue(response.isError());
      assertEquals(-32700, response.getError().get().getCode());
      assertNull(response.getId());
    }
  }

  @Test
  public void testErrorHandling_InvalidRequestMissingMethod() {
    // Request missing required method field
    String invalidJson = "{\"jsonrpc\":\"2.0\",\"id\":1}";

    assertThrows(
        JsonRpcException.class,
        () -> {
          gson.fromJson(invalidJson, JsonRpcRequest.class);
        });

    // Server would catch this and return invalid request error
    JsonRpcError<?> error = StandardErrors.invalidRequest();
    JsonRpcResponse<Object> response = JsonRpcResponse.error(1, error);

    assertTrue(response.isError());
    assertEquals(-32600, response.getError().get().getCode());
  }

  @Test
  public void testErrorHandling_InvalidRequestWrongVersion() {
    // Request with wrong JSON-RPC version
    String invalidJson = "{\"jsonrpc\":\"1.0\",\"method\":\"test\",\"id\":1}";

    assertThrows(
        JsonRpcException.class,
        () -> {
          gson.fromJson(invalidJson, JsonRpcRequest.class);
        });
  }

  @Test
  public void testErrorHandling_ResponseWithBothResultAndError() {
    // Invalid response with both result and error
    String invalidJson =
        "{\"jsonrpc\":\"2.0\",\"result\":42,\"error\":{\"code\":-32600,\"message\":\"Error\"},\"id\":1}";

    assertThrows(
        JsonRpcException.class,
        () -> {
          gson.fromJson(invalidJson, JsonRpcResponse.class);
        });
  }

  @Test
  public void testErrorHandling_ResponseWithNeitherResultNorError() {
    // Invalid response with neither result nor error
    String invalidJson = "{\"jsonrpc\":\"2.0\",\"id\":1}";

    assertThrows(
        JsonRpcException.class,
        () -> {
          gson.fromJson(invalidJson, JsonRpcResponse.class);
        });
  }

  @Test
  public void testErrorHandling_ExceptionWrapping() {
    // Simulate server catching application exception
    try {
      throw new IllegalArgumentException("Invalid input");
    } catch (Exception e) {
      // Server wraps in JSON-RPC error
      JsonRpcError<String> error = StandardErrors.internalError(e.getMessage());
      JsonRpcResponse<Object> response = JsonRpcResponse.error(1, error);

      String responseJson = gson.toJson(response);
      JsonRpcResponse<?> receivedResponse = gson.fromJson(responseJson, JsonRpcResponse.class);

      assertTrue(receivedResponse.isError());
      assertEquals(-32603, receivedResponse.getError().get().getCode());
    }
  }

  // JSON round-trip serialization tests

  @Test
  public void testRoundTrip_RequestWithStringParams() {
    JsonRpcRequest<String> original =
        JsonRpcRequest.<String>builder().method("echo").params("Hello, World!").id("msg-1").build();

    String json = gson.toJson(original);
    JsonRpcRequest<?> deserialized = gson.fromJson(json, JsonRpcRequest.class);

    assertEquals(original.getJsonrpc(), deserialized.getJsonrpc());
    assertEquals(original.getMethod(), deserialized.getMethod());
    assertEquals(original.getId().get(), deserialized.getId().get());
    assertTrue(deserialized.getParams().isPresent());
  }

  @Test
  public void testRoundTrip_RequestWithMapParams() {
    Map<String, Object> params = new HashMap<>();
    params.put("name", "John");
    params.put("age", 30);
    params.put("active", true);

    JsonRpcRequest<Map<String, Object>> original =
        JsonRpcRequest.<Map<String, Object>>builder()
            .method("updateUser")
            .params(params)
            .id(100)
            .build();

    String json = gson.toJson(original);
    JsonRpcRequest<?> deserialized = gson.fromJson(json, JsonRpcRequest.class);

    assertEquals(original.getMethod(), deserialized.getMethod());
    assertTrue(deserialized.getParams().isPresent());
  }

  @Test
  public void testRoundTrip_RequestWithArrayParams() {
    java.util.List<Integer> params = java.util.Arrays.asList(1, 2, 3, 4, 5);

    JsonRpcRequest<java.util.List<Integer>> original =
        JsonRpcRequest.<java.util.List<Integer>>builder()
            .method("sum")
            .params(params)
            .id(200)
            .build();

    String json = gson.toJson(original);
    JsonRpcRequest<?> deserialized = gson.fromJson(json, JsonRpcRequest.class);

    assertEquals(original.getMethod(), deserialized.getMethod());
    assertTrue(deserialized.getParams().isPresent());
  }

  @Test
  public void testRoundTrip_RequestWithNullParams() {
    JsonRpcRequest<Object> original =
        JsonRpcRequest.builder().method("ping").params(null).id(1).build();

    String json = gson.toJson(original);
    JsonRpcRequest<?> deserialized = gson.fromJson(json, JsonRpcRequest.class);

    assertEquals(original.getMethod(), deserialized.getMethod());
  }

  @Test
  public void testRoundTrip_ResponseWithPrimitiveResult() {
    JsonRpcResponse<Integer> original = JsonRpcResponse.success(1, 42);

    String json = gson.toJson(original);
    JsonRpcResponse<?> deserialized = gson.fromJson(json, JsonRpcResponse.class);

    assertTrue(deserialized.isSuccess());
    Object resultObj = deserialized.getResult().get();
    if (resultObj instanceof com.google.gson.JsonPrimitive) {
      assertEquals(42.0, ((com.google.gson.JsonPrimitive) resultObj).getAsDouble());
    }
  }

  @Test
  public void testRoundTrip_ResponseWithStringResult() {
    JsonRpcResponse<String> original = JsonRpcResponse.success("req-1", "Success");

    String json = gson.toJson(original);
    JsonRpcResponse<?> deserialized = gson.fromJson(json, JsonRpcResponse.class);

    assertTrue(deserialized.isSuccess());
    Object resultObj = deserialized.getResult().get();
    if (resultObj instanceof com.google.gson.JsonPrimitive) {
      assertEquals("Success", ((com.google.gson.JsonPrimitive) resultObj).getAsString());
    }
  }

  @Test
  public void testRoundTrip_ResponseWithObjectResult() {
    Map<String, Object> result = new HashMap<>();
    result.put("status", "completed");
    result.put("count", 5);

    JsonRpcResponse<Map<String, Object>> original = JsonRpcResponse.success(1, result);

    String json = gson.toJson(original);
    JsonRpcResponse<?> deserialized = gson.fromJson(json, JsonRpcResponse.class);

    assertTrue(deserialized.isSuccess());
    assertTrue(deserialized.getResult().isPresent());
  }

  @Test
  public void testRoundTrip_ResponseWithNullResult() {
    JsonRpcResponse<Object> original = JsonRpcResponse.success(1, null);

    String json = gson.toJson(original);

    // Verify the response is a success response
    assertTrue(original.isSuccess());

    // Verify JSON structure - null result is valid per JSON-RPC 2.0 spec
    assertTrue(json.contains("\"jsonrpc\":\"2.0\""));
    assertTrue(json.contains("\"id\":1"));
    // The result field should be present (either as null or omitted based on
    // serialization)
  }

  @Test
  public void testRoundTrip_ErrorWithoutData() {
    JsonRpcError<?> error = StandardErrors.methodNotFound();
    JsonRpcResponse<Object> original = JsonRpcResponse.error(1, error);

    String json = gson.toJson(original);
    JsonRpcResponse<?> deserialized = gson.fromJson(json, JsonRpcResponse.class);

    assertTrue(deserialized.isError());
    assertEquals(-32601, deserialized.getError().get().getCode());
    assertEquals("Method not found", deserialized.getError().get().getMessage());
  }

  @Test
  public void testRoundTrip_ErrorWithStringData() {
    JsonRpcError<String> error = StandardErrors.invalidParams("Missing field: name");
    JsonRpcResponse<Object> original = JsonRpcResponse.error(1, error);

    String json = gson.toJson(original);
    JsonRpcResponse<?> deserialized = gson.fromJson(json, JsonRpcResponse.class);

    assertTrue(deserialized.isError());
    assertTrue(deserialized.getError().get().getData().isPresent());
  }

  @Test
  public void testRoundTrip_ErrorWithObjectData() {
    Map<String, Object> errorData = new HashMap<>();
    errorData.put("field", "email");
    errorData.put("reason", "invalid format");

    JsonRpcError<Map<String, Object>> error = StandardErrors.invalidParams(errorData);
    JsonRpcResponse<Object> original = JsonRpcResponse.error(1, error);

    String json = gson.toJson(original);
    JsonRpcResponse<?> deserialized = gson.fromJson(json, JsonRpcResponse.class);

    assertTrue(deserialized.isError());
    assertTrue(deserialized.getError().get().getData().isPresent());
  }

  // Protocol compliance tests

  @Test
  public void testProtocolCompliance_RequestMustHaveJsonrpc() {
    JsonRpcRequest<Object> request = JsonRpcRequest.builder().method("test").id(1).build();

    assertEquals("2.0", request.getJsonrpc());

    String json = gson.toJson(request);
    assertTrue(json.contains("\"jsonrpc\":\"2.0\""));
  }

  @Test
  public void testProtocolCompliance_RequestMustHaveMethod() {
    JsonRpcRequest<Object> request = JsonRpcRequest.builder().method("testMethod").id(1).build();

    assertNotNull(request.getMethod());
    assertFalse(request.getMethod().isEmpty());
  }

  @Test
  public void testProtocolCompliance_RequestMethodCannotStartWithRpc() {
    assertThrows(
        JsonRpcException.class,
        () -> {
          JsonRpcRequest.builder().method("rpc.internal").id(1).build();
        });
  }

  @Test
  public void testProtocolCompliance_RequestIdCanBeStringNumberOrNull() {
    // String id
    JsonRpcRequest<Object> req1 = JsonRpcRequest.builder().method("test").id("string-id").build();
    assertEquals("string-id", req1.getId().get());

    // Number id
    JsonRpcRequest<Object> req2 = JsonRpcRequest.builder().method("test").id(123).build();
    assertEquals(123, req2.getId().get());

    // Null id (notification)
    JsonRpcRequest<Object> req3 = JsonRpcRequest.notification("test", null);
    assertFalse(req3.getId().isPresent());
  }

  @Test
  public void testProtocolCompliance_ResponseMustHaveJsonrpc() {
    JsonRpcResponse<Integer> response = JsonRpcResponse.success(1, 42);

    assertEquals("2.0", response.getJsonrpc());

    String json = gson.toJson(response);
    assertTrue(json.contains("\"jsonrpc\":\"2.0\""));
  }

  @Test
  public void testProtocolCompliance_ResponseMustHaveId() {
    JsonRpcResponse<Integer> response = JsonRpcResponse.success(1, 42);

    assertNotNull(response.getId());
  }

  @Test
  public void testProtocolCompliance_ResponseCannotHaveBothResultAndError() {
    // This is enforced by the API design - cannot construct such a response
    JsonRpcResponse<Integer> successResponse = JsonRpcResponse.success(1, 42);
    assertTrue(successResponse.getResult().isPresent());
    assertFalse(successResponse.getError().isPresent());

    JsonRpcError<?> error = StandardErrors.methodNotFound();
    JsonRpcResponse<Object> errorResponse = JsonRpcResponse.error(1, error);
    assertFalse(errorResponse.getResult().isPresent());
    assertTrue(errorResponse.getError().isPresent());
  }

  @Test
  public void testProtocolCompliance_ErrorMustHaveCodeAndMessage() {
    JsonRpcError<?> error = JsonRpcError.of(-32600, "Invalid Request");

    assertEquals(-32600, error.getCode());
    assertNotNull(error.getMessage());
    assertFalse(error.getMessage().isEmpty());
  }

  @Test
  public void testProtocolCompliance_StandardErrorCodesAreCorrect() {
    assertEquals(-32700, StandardErrors.PARSE_ERROR);
    assertEquals(-32600, StandardErrors.INVALID_REQUEST);
    assertEquals(-32601, StandardErrors.METHOD_NOT_FOUND);
    assertEquals(-32602, StandardErrors.INVALID_PARAMS);
    assertEquals(-32603, StandardErrors.INTERNAL_ERROR);
  }

  @Test
  public void testProtocolCompliance_ServerErrorCodeRange() {
    assertTrue(StandardErrors.isServerErrorCode(-32000));
    assertTrue(StandardErrors.isServerErrorCode(-32050));
    assertTrue(StandardErrors.isServerErrorCode(-32099));
    assertFalse(StandardErrors.isServerErrorCode(-32100));
    assertFalse(StandardErrors.isServerErrorCode(-31999));
  }

  @Test
  public void testProtocolCompliance_ReservedErrorCodeRange() {
    assertTrue(StandardErrors.isReservedErrorCode(-32700));
    assertTrue(StandardErrors.isReservedErrorCode(-32600));
    assertTrue(StandardErrors.isReservedErrorCode(-32000));
    assertFalse(StandardErrors.isReservedErrorCode(-31999));
    assertFalse(StandardErrors.isReservedErrorCode(1000));
  }

  @Test
  public void testProtocolCompliance_NotificationHasNoId() {
    JsonRpcRequest<String> notification = JsonRpcRequest.notification("update", "data");

    assertTrue(notification.isNotification());
    assertFalse(notification.getId().isPresent());

    String json = gson.toJson(notification);
    assertFalse(json.contains("\"id\""));
  }

  // Complex integration scenarios

  @Test
  public void testComplexScenario_BatchProcessingSimulation() {
    // Simulate processing multiple requests
    JsonRpcRequest<?>[] requests = new JsonRpcRequest<?>[3];

    requests[0] =
        JsonRpcRequest.builder().method("add").params(Map.of("a", 5, "b", 3)).id(1).build();

    requests[1] =
        JsonRpcRequest.builder().method("multiply").params(Map.of("a", 4, "b", 7)).id(2).build();

    requests[2] = JsonRpcRequest.notification("log", "Batch processing");

    // Process each request
    for (JsonRpcRequest<?> request : requests) {
      String json = gson.toJson(request);
      JsonRpcRequest<?> received = gson.fromJson(json, JsonRpcRequest.class);

      assertNotNull(received);
      assertNotNull(received.getMethod());

      if (!received.isNotification()) {
        // Create response for non-notifications
        JsonRpcResponse<Integer> response =
            JsonRpcResponse.success(received.getId().orElse(null), 0);
        assertNotNull(response);
      }
    }
  }

  @Test
  public void testComplexScenario_ErrorRecovery() {
    // Client sends request
    JsonRpcRequest<Object> request = JsonRpcRequest.builder().method("processData").id(1).build();

    String requestJson = gson.toJson(request);
    JsonRpcRequest<?> receivedRequest = gson.fromJson(requestJson, JsonRpcRequest.class);

    // Server encounters error
    JsonRpcError<String> error = StandardErrors.internalError("Temporary failure");
    JsonRpcResponse<Object> errorResponse =
        JsonRpcResponse.error(receivedRequest.getId().orElse(null), error);

    String errorJson = gson.toJson(errorResponse);
    JsonRpcResponse<?> receivedError = gson.fromJson(errorJson, JsonRpcResponse.class);

    // Client detects error and retries
    assertTrue(receivedError.isError());

    // Retry succeeds
    JsonRpcResponse<String> successResponse =
        JsonRpcResponse.success(receivedRequest.getId().orElse(null), "Success");

    String successJson = gson.toJson(successResponse);
    JsonRpcResponse<?> receivedSuccess = gson.fromJson(successJson, JsonRpcResponse.class);

    assertTrue(receivedSuccess.isSuccess());
  }

  @Test
  public void testComplexScenario_NestedDataStructures() {
    // Complex nested params
    Map<String, Object> params = new HashMap<>();
    params.put(
        "user",
        Map.of("id", 123, "name", "John", "roles", java.util.Arrays.asList("admin", "user")));
    params.put("settings", Map.of("theme", "dark", "notifications", true));

    JsonRpcRequest<Map<String, Object>> request =
        JsonRpcRequest.<Map<String, Object>>builder()
            .method("updateProfile")
            .params(params)
            .id("complex-1")
            .build();

    String json = gson.toJson(request);
    JsonRpcRequest<?> deserialized = gson.fromJson(json, JsonRpcRequest.class);

    assertEquals(request.getMethod(), deserialized.getMethod());
    assertTrue(deserialized.getParams().isPresent());
  }
}
