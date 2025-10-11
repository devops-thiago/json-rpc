package br.com.arquivolivre;

import static org.junit.jupiter.api.Assertions.*;

import br.com.arquivolivre.gson.JsonRpcGsonConfig;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Comprehensive unit tests for Gson serialization/deserialization of JSON-RPC objects. */
public class TestJsonRpcGson {

  private Gson gson;

  @BeforeEach
  public void setUp() {
    gson = JsonRpcGsonConfig.createGson();
  }

  // JsonRpcRequest serialization tests

  @Test
  public void testRequest_Serialize_AllFields() {
    Map<String, Object> params = new HashMap<>();
    params.put("minuend", 42);
    params.put("subtrahend", 23);

    JsonRpcRequest<Map<String, Object>> request =
        JsonRpcRequest.<Map<String, Object>>builder()
            .method("subtract")
            .params(params)
            .id(1)
            .build();

    String json = gson.toJson(request);

    assertTrue(json.contains("\"jsonrpc\":\"2.0\""));
    assertTrue(json.contains("\"method\":\"subtract\""));
    assertTrue(json.contains("\"id\":1"));
    assertTrue(json.contains("\"params\""));
  }

  @Test
  public void testRequest_Serialize_Notification() {
    JsonRpcRequest<String> notification = JsonRpcRequest.notification("notify", "hello");

    String json = gson.toJson(notification);

    assertTrue(json.contains("\"jsonrpc\":\"2.0\""));
    assertTrue(json.contains("\"method\":\"notify\""));
    assertTrue(json.contains("\"params\":\"hello\""));
    assertFalse(json.contains("\"id\""));
  }

  @Test
  public void testRequest_Serialize_StringId() {
    JsonRpcRequest<Object> request =
        JsonRpcRequest.builder().method("test").id("string-id").build();

    String json = gson.toJson(request);

    assertTrue(json.contains("\"id\":\"string-id\""));
  }

  @Test
  public void testRequest_Serialize_NoParams() {
    JsonRpcRequest<Object> request = JsonRpcRequest.builder().method("test").id(1).build();

    String json = gson.toJson(request);

    assertTrue(json.contains("\"method\":\"test\""));
    assertFalse(json.contains("\"params\""));
  }

  // JsonRpcRequest deserialization tests

  @Test
  public void testRequest_Deserialize_AllFields() {
    String json =
        "{\"jsonrpc\":\"2.0\",\"method\":\"subtract\",\"params\":{\"minuend\":42,\"subtrahend\":23},\"id\":1}";

    JsonRpcRequest<?> request = gson.fromJson(json, JsonRpcRequest.class);

    assertEquals("2.0", request.getJsonrpc());
    assertEquals("subtract", request.getMethod());
    assertTrue(request.getParams().isPresent());
    assertTrue(request.getId().isPresent());
    assertEquals(1.0, ((Number) request.getId().get()).doubleValue());
  }

  @Test
  public void testRequest_Deserialize_Notification() {
    String json = "{\"jsonrpc\":\"2.0\",\"method\":\"notify\",\"params\":\"hello\"}";

    JsonRpcRequest<?> request = gson.fromJson(json, JsonRpcRequest.class);

    assertEquals("notify", request.getMethod());
    assertTrue(request.isNotification());
    assertFalse(request.getId().isPresent());
  }

  @Test
  public void testRequest_Deserialize_StringId() {
    String json = "{\"jsonrpc\":\"2.0\",\"method\":\"test\",\"id\":\"string-id\"}";

    JsonRpcRequest<?> request = gson.fromJson(json, JsonRpcRequest.class);

    assertTrue(request.getId().isPresent());
    assertEquals("string-id", request.getId().get());
  }

  @Test
  public void testRequest_Deserialize_InvalidMissingJsonrpc() {
    String json = "{\"method\":\"test\",\"id\":1}";

    assertThrows(JsonRpcException.class, () -> gson.fromJson(json, JsonRpcRequest.class));
  }

  @Test
  public void testRequest_Deserialize_InvalidMissingMethod() {
    String json = "{\"jsonrpc\":\"2.0\",\"id\":1}";

    assertThrows(JsonRpcException.class, () -> gson.fromJson(json, JsonRpcRequest.class));
  }

  @Test
  public void testRequest_Deserialize_InvalidJsonrpcVersion() {
    String json = "{\"jsonrpc\":\"1.0\",\"method\":\"test\",\"id\":1}";

    assertThrows(JsonRpcException.class, () -> gson.fromJson(json, JsonRpcRequest.class));
  }

  // JsonRpcResponse serialization tests

  @Test
  public void testResponse_Serialize_Success() {
    JsonRpcResponse<Integer> response = JsonRpcResponse.success(1, 19);

    String json = gson.toJson(response);

    assertTrue(json.contains("\"jsonrpc\":\"2.0\""));
    assertTrue(json.contains("\"result\":19"));
    assertTrue(json.contains("\"id\":1"));
    assertFalse(json.contains("\"error\""));
  }

  @Test
  public void testResponse_Serialize_Error() {
    JsonRpcError<?> error = StandardErrors.methodNotFound();
    JsonRpcResponse<Object> response = JsonRpcResponse.error(1, error);

    String json = gson.toJson(response);

    assertTrue(json.contains("\"jsonrpc\":\"2.0\""));
    assertTrue(json.contains("\"error\""));
    assertTrue(json.contains("\"code\":-32601"));
    assertTrue(json.contains("\"message\":\"Method not found\""));
    assertTrue(json.contains("\"id\":1"));
    assertFalse(json.contains("\"result\""));
  }

  @Test
  public void testResponse_Serialize_ErrorWithData() {
    JsonRpcError<String> error = StandardErrors.invalidParams("Missing required field");
    JsonRpcResponse<Object> response = JsonRpcResponse.error(1, error);

    String json = gson.toJson(response);

    assertTrue(json.contains("\"error\""));
    assertTrue(json.contains("\"data\":\"Missing required field\""));
  }

  @Test
  public void testResponse_Serialize_NullId() {
    JsonRpcResponse<String> response = JsonRpcResponse.success(null, "result");

    String json = gson.toJson(response);

    // Gson may omit null fields or include them, both are valid
    assertTrue(json.contains("\"result\":\"result\""));
  }

  // JsonRpcResponse deserialization tests

  @Test
  public void testResponse_Deserialize_Success() {
    String json = "{\"jsonrpc\":\"2.0\",\"result\":19,\"id\":1}";

    JsonRpcResponse<?> response = gson.fromJson(json, JsonRpcResponse.class);

    assertEquals("2.0", response.getJsonrpc());
    assertTrue(response.isSuccess());
    assertTrue(response.getResult().isPresent());
    assertNotNull(response.getResult().get());
    assertNotNull(response.getId());
  }

  @Test
  public void testResponse_Deserialize_Error() {
    String json =
        "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32601,\"message\":\"Method not found\"},\"id\":1}";

    JsonRpcResponse<?> response = gson.fromJson(json, JsonRpcResponse.class);

    assertTrue(response.isError());
    assertTrue(response.getError().isPresent());
    assertEquals(-32601, response.getError().get().getCode());
    assertEquals("Method not found", response.getError().get().getMessage());
  }

  @Test
  public void testResponse_Deserialize_ErrorWithData() {
    String json =
        "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32602,\"message\":\"Invalid params\",\"data\":\"Additional info\"},\"id\":1}";

    JsonRpcResponse<?> response = gson.fromJson(json, JsonRpcResponse.class);

    assertTrue(response.isError());
    assertTrue(response.getError().get().getData().isPresent());
  }

  @Test
  public void testResponse_Deserialize_InvalidBothResultAndError() {
    String json =
        "{\"jsonrpc\":\"2.0\",\"result\":19,\"error\":{\"code\":-32601,\"message\":\"Not found\"},\"id\":1}";

    assertThrows(JsonRpcException.class, () -> gson.fromJson(json, JsonRpcResponse.class));
  }

  @Test
  public void testResponse_Deserialize_InvalidNeitherResultNorError() {
    String json = "{\"jsonrpc\":\"2.0\",\"id\":1}";

    assertThrows(JsonRpcException.class, () -> gson.fromJson(json, JsonRpcResponse.class));
  }

  // JsonRpcError serialization tests

  @Test
  public void testError_Serialize_NoData() {
    JsonRpcError<?> error = JsonRpcError.of(-32600, "Invalid Request");

    String json = gson.toJson(error);

    assertTrue(json.contains("\"code\":-32600"));
    assertTrue(json.contains("\"message\":\"Invalid Request\""));
    assertFalse(json.contains("\"data\""));
  }

  @Test
  public void testError_Serialize_WithData() {
    JsonRpcError<String> error = JsonRpcError.of(-32602, "Invalid params", "Missing field");

    String json = gson.toJson(error);

    assertTrue(json.contains("\"code\":-32602"));
    assertTrue(json.contains("\"message\":\"Invalid params\""));
    assertTrue(json.contains("\"data\":\"Missing field\""));
  }

  @Test
  public void testError_Serialize_WithComplexData() {
    Map<String, Object> data = new HashMap<>();
    data.put("expected", 2);
    data.put("actual", 1);

    JsonRpcError<Map<String, Object>> error = JsonRpcError.of(-32602, "Invalid params", data);

    String json = gson.toJson(error);

    assertTrue(json.contains("\"data\""));
    assertTrue(json.contains("\"expected\""));
    assertTrue(json.contains("\"actual\""));
  }

  // JsonRpcError deserialization tests

  @Test
  public void testError_Deserialize_NoData() {
    String json = "{\"code\":-32600,\"message\":\"Invalid Request\"}";

    JsonRpcError<?> error = gson.fromJson(json, JsonRpcError.class);

    assertEquals(-32600, error.getCode());
    assertEquals("Invalid Request", error.getMessage());
    assertFalse(error.getData().isPresent());
  }

  @Test
  public void testError_Deserialize_WithData() {
    String json = "{\"code\":-32602,\"message\":\"Invalid params\",\"data\":\"Missing field\"}";

    JsonRpcError<?> error = gson.fromJson(json, JsonRpcError.class);

    assertEquals(-32602, error.getCode());
    assertEquals("Invalid params", error.getMessage());
    assertTrue(error.getData().isPresent());
  }

  @Test
  public void testError_Deserialize_InvalidMissingCode() {
    String json = "{\"message\":\"Error message\"}";

    assertThrows(JsonRpcException.class, () -> gson.fromJson(json, JsonRpcError.class));
  }

  @Test
  public void testError_Deserialize_InvalidMissingMessage() {
    String json = "{\"code\":-32600}";

    assertThrows(JsonRpcException.class, () -> gson.fromJson(json, JsonRpcError.class));
  }

  // Round-trip tests

  @Test
  public void testRoundTrip_Request() {
    JsonRpcRequest<String> original =
        JsonRpcRequest.<String>builder().method("test").params("data").id(1).build();

    String json = gson.toJson(original);
    JsonRpcRequest<?> deserialized = gson.fromJson(json, JsonRpcRequest.class);

    assertEquals(original.getJsonrpc(), deserialized.getJsonrpc());
    assertEquals(original.getMethod(), deserialized.getMethod());
    assertTrue(deserialized.getParams().isPresent());
    assertTrue(deserialized.getId().isPresent());
  }

  @Test
  public void testRoundTrip_SuccessResponse() {
    JsonRpcResponse<Integer> original = JsonRpcResponse.success(1, 42);

    String json = gson.toJson(original);
    JsonRpcResponse<?> deserialized = gson.fromJson(json, JsonRpcResponse.class);

    assertEquals(original.getJsonrpc(), deserialized.getJsonrpc());
    assertTrue(deserialized.isSuccess());
    assertTrue(deserialized.getResult().isPresent());
  }

  @Test
  public void testRoundTrip_ErrorResponse() {
    JsonRpcError<?> error = StandardErrors.methodNotFound();
    JsonRpcResponse<Object> original = JsonRpcResponse.error(1, error);

    String json = gson.toJson(original);
    JsonRpcResponse<?> deserialized = gson.fromJson(json, JsonRpcResponse.class);

    assertEquals(original.getJsonrpc(), deserialized.getJsonrpc());
    assertTrue(deserialized.isError());
    assertEquals(-32601, deserialized.getError().get().getCode());
  }

  @Test
  public void testRoundTrip_Error() {
    JsonRpcError<String> original = JsonRpcError.of(-32602, "Invalid params", "test data");

    String json = gson.toJson(original);
    JsonRpcError<?> deserialized = gson.fromJson(json, JsonRpcError.class);

    assertEquals(original.getCode(), deserialized.getCode());
    assertEquals(original.getMessage(), deserialized.getMessage());
    assertTrue(deserialized.getData().isPresent());
  }

  // Edge cases

  @Test
  public void testEdgeCase_NullResult() {
    JsonRpcResponse<Object> response = JsonRpcResponse.success(1, null);

    String json = gson.toJson(response);

    // Verify serialization works
    assertTrue(json.contains("\"jsonrpc\":\"2.0\""));
    assertTrue(json.contains("\"id\":1"));
  }

  @Test
  public void testEdgeCase_ComplexParams() {
    Map<String, Object> params = new HashMap<>();
    params.put("nested", Map.of("key", "value"));
    params.put("array", java.util.Arrays.asList(1, 2, 3));

    JsonRpcRequest<Map<String, Object>> request =
        JsonRpcRequest.<Map<String, Object>>builder()
            .method("complex")
            .params(params)
            .id(1)
            .build();

    String json = gson.toJson(request);
    JsonRpcRequest<?> deserialized = gson.fromJson(json, JsonRpcRequest.class);

    assertTrue(deserialized.getParams().isPresent());
  }
}
