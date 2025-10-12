package br.com.arquivolivre.jsonrpc;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Comprehensive unit tests for JsonRpcResponse. */
public class TestJsonRpcResponse {

  // Success response tests

  @Test
  public void testSuccessResponse_IntegerResult() {
    JsonRpcResponse<Integer> response = JsonRpcResponse.success(1, 42);

    assertEquals("2.0", response.getJsonrpc());
    assertEquals(1, response.getId());
    assertTrue(response.getResult().isPresent());
    assertEquals(42, response.getResult().get());
    assertFalse(response.getError().isPresent());
    assertTrue(response.isSuccess());
    assertFalse(response.isError());
  }

  @Test
  public void testSuccessResponse_StringResult() {
    JsonRpcResponse<String> response = JsonRpcResponse.success("req-1", "success");

    assertEquals("2.0", response.getJsonrpc());
    assertEquals("req-1", response.getId());
    assertTrue(response.getResult().isPresent());
    assertEquals("success", response.getResult().get());
    assertTrue(response.isSuccess());
  }

  @Test
  public void testSuccessResponse_ComplexResult() {
    Map<String, Object> result = new HashMap<>();
    result.put("status", "ok");
    result.put("count", 5);

    JsonRpcResponse<Map<String, Object>> response = JsonRpcResponse.success(1, result);

    assertTrue(response.getResult().isPresent());
    assertEquals(result, response.getResult().get());
  }

  @Test
  public void testSuccessResponse_NullResult() {
    // null result is valid for success responses
    JsonRpcResponse<Object> response = JsonRpcResponse.success(1, null);

    assertTrue(response.isSuccess());
    assertFalse(response.getResult().isPresent());
    assertFalse(response.getError().isPresent());
  }

  @Test
  public void testSuccessResponse_NullId() {
    JsonRpcResponse<String> response = JsonRpcResponse.success(null, "result");

    assertNull(response.getId());
    assertTrue(response.getResult().isPresent());
    assertEquals("result", response.getResult().get());
    assertTrue(response.isSuccess());
  }

  @Test
  public void testSuccessResponse_ZeroId() {
    JsonRpcResponse<String> response = JsonRpcResponse.success(0, "result");

    assertEquals(0, response.getId());
    assertTrue(response.isSuccess());
  }

  // Error response tests

  @Test
  public void testErrorResponse_StandardError() {
    JsonRpcError<?> error = JsonRpcError.of(-32601, "Method not found");
    JsonRpcResponse<Object> response = JsonRpcResponse.error(1, error);

    assertEquals("2.0", response.getJsonrpc());
    assertEquals(1, response.getId());
    assertFalse(response.getResult().isPresent());
    assertTrue(response.getError().isPresent());
    assertEquals(error, response.getError().get());
    assertFalse(response.isSuccess());
    assertTrue(response.isError());
  }

  @Test
  public void testErrorResponse_WithData() {
    JsonRpcError<String> error =
        JsonRpcError.of(-32602, "Invalid params", "Missing required field");
    JsonRpcResponse<Object> response = JsonRpcResponse.error(1, error);

    assertTrue(response.isError());
    assertTrue(response.getError().isPresent());
    assertTrue(response.getError().get().getData().isPresent());
  }

  @Test
  public void testErrorResponse_NullId() {
    JsonRpcError<?> error = JsonRpcError.of(-32700, "Parse error");
    JsonRpcResponse<Object> response = JsonRpcResponse.error(null, error);

    assertNull(response.getId());
    assertTrue(response.getError().isPresent());
    assertTrue(response.isError());
  }

  @Test
  public void testErrorResponse_StringId() {
    JsonRpcError<?> error = StandardErrors.internalError();
    JsonRpcResponse<Object> response = JsonRpcResponse.error("req-1", error);

    assertEquals("req-1", response.getId());
    assertTrue(response.isError());
  }

  // Validation tests

  @Test
  public void testValidation_NullError() {
    JsonRpcException exception =
        assertThrows(JsonRpcException.class, () -> JsonRpcResponse.error(1, null));
    assertNotNull(exception.getError());
  }

  // Mutual exclusivity tests

  @Test
  public void testMutualExclusivity_SuccessHasNoError() {
    JsonRpcResponse<Integer> response = JsonRpcResponse.success(1, 42);

    assertTrue(response.getResult().isPresent());
    assertFalse(response.getError().isPresent());
  }

  @Test
  public void testMutualExclusivity_ErrorHasNoResult() {
    JsonRpcError<?> error = StandardErrors.methodNotFound();
    JsonRpcResponse<Object> response = JsonRpcResponse.error(1, error);

    assertFalse(response.getResult().isPresent());
    assertTrue(response.getError().isPresent());
  }

  // ID correlation tests

  @Test
  public void testIdCorrelation_IntegerId() {
    JsonRpcResponse<String> response = JsonRpcResponse.success(123, "result");
    assertEquals(123, response.getId());
  }

  @Test
  public void testIdCorrelation_StringId() {
    JsonRpcResponse<String> response = JsonRpcResponse.success("abc-123", "result");
    assertEquals("abc-123", response.getId());
  }

  @Test
  public void testIdCorrelation_NegativeId() {
    JsonRpcResponse<String> response = JsonRpcResponse.success(-1, "result");
    assertEquals(-1, response.getId());
  }

  // Standard error responses

  @Test
  public void testStandardError_ParseError() {
    JsonRpcResponse<Object> response = JsonRpcResponse.error(null, StandardErrors.parseError());

    assertTrue(response.isError());
    assertEquals(-32700, response.getError().get().getCode());
  }

  @Test
  public void testStandardError_InvalidRequest() {
    JsonRpcResponse<Object> response = JsonRpcResponse.error(1, StandardErrors.invalidRequest());

    assertTrue(response.isError());
    assertEquals(-32600, response.getError().get().getCode());
  }

  @Test
  public void testStandardError_MethodNotFound() {
    JsonRpcResponse<Object> response = JsonRpcResponse.error(1, StandardErrors.methodNotFound());

    assertTrue(response.isError());
    assertEquals(-32601, response.getError().get().getCode());
  }

  @Test
  public void testStandardError_InvalidParams() {
    JsonRpcResponse<Object> response = JsonRpcResponse.error(1, StandardErrors.invalidParams());

    assertTrue(response.isError());
    assertEquals(-32602, response.getError().get().getCode());
  }

  @Test
  public void testStandardError_InternalError() {
    JsonRpcResponse<Object> response = JsonRpcResponse.error(1, StandardErrors.internalError());

    assertTrue(response.isError());
    assertEquals(-32603, response.getError().get().getCode());
  }

  // Edge cases

  @Test
  public void testEdgeCase_EmptyStringResult() {
    JsonRpcResponse<String> response = JsonRpcResponse.success(1, "");

    assertTrue(response.getResult().isPresent());
    assertEquals("", response.getResult().get());
  }

  @Test
  public void testEdgeCase_BooleanResult() {
    JsonRpcResponse<Boolean> response = JsonRpcResponse.success(1, true);

    assertTrue(response.getResult().isPresent());
    assertEquals(true, response.getResult().get());
  }

  @Test
  public void testEdgeCase_ListResult() {
    java.util.List<String> result = java.util.Arrays.asList("a", "b", "c");
    JsonRpcResponse<java.util.List<String>> response = JsonRpcResponse.success(1, result);

    assertTrue(response.getResult().isPresent());
    assertEquals(result, response.getResult().get());
  }
}
