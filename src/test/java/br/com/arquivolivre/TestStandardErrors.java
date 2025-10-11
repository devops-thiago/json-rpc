package br.com.arquivolivre;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Comprehensive unit tests for StandardErrors. */
public class TestStandardErrors {

  // Test standard error constants

  @Test
  public void testErrorCodeConstants() {
    assertEquals(-32700, StandardErrors.PARSE_ERROR);
    assertEquals(-32600, StandardErrors.INVALID_REQUEST);
    assertEquals(-32601, StandardErrors.METHOD_NOT_FOUND);
    assertEquals(-32602, StandardErrors.INVALID_PARAMS);
    assertEquals(-32603, StandardErrors.INTERNAL_ERROR);
  }

  @Test
  public void testErrorRangeConstants() {
    assertEquals(-32099, StandardErrors.SERVER_ERROR_MIN);
    assertEquals(-32000, StandardErrors.SERVER_ERROR_MAX);
    assertEquals(-32768, StandardErrors.RESERVED_ERROR_MIN);
    assertEquals(-32000, StandardErrors.RESERVED_ERROR_MAX);
  }

  // Test parseError factory methods

  @Test
  public void testParseError_NoData() {
    JsonRpcError<?> error = StandardErrors.parseError();

    assertEquals(-32700, error.getCode());
    assertEquals("Parse error", error.getMessage());
    assertFalse(error.getData().isPresent());
  }

  @Test
  public void testParseError_WithData() {
    String data = "Invalid JSON syntax";
    JsonRpcError<String> error = StandardErrors.parseError(data);

    assertEquals(-32700, error.getCode());
    assertEquals("Parse error", error.getMessage());
    assertTrue(error.getData().isPresent());
    assertEquals(data, error.getData().get());
  }

  // Test invalidRequest factory methods

  @Test
  public void testInvalidRequest_NoData() {
    JsonRpcError<?> error = StandardErrors.invalidRequest();

    assertEquals(-32600, error.getCode());
    assertEquals("Invalid Request", error.getMessage());
    assertFalse(error.getData().isPresent());
  }

  @Test
  public void testInvalidRequest_WithData() {
    String data = "Missing required field";
    JsonRpcError<String> error = StandardErrors.invalidRequest(data);

    assertEquals(-32600, error.getCode());
    assertEquals("Invalid Request", error.getMessage());
    assertTrue(error.getData().isPresent());
    assertEquals(data, error.getData().get());
  }

  // Test methodNotFound factory methods

  @Test
  public void testMethodNotFound_NoData() {
    JsonRpcError<?> error = StandardErrors.methodNotFound();

    assertEquals(-32601, error.getCode());
    assertEquals("Method not found", error.getMessage());
    assertFalse(error.getData().isPresent());
  }

  @Test
  public void testMethodNotFound_WithData() {
    String data = "Method 'unknown' does not exist";
    JsonRpcError<String> error = StandardErrors.methodNotFound(data);

    assertEquals(-32601, error.getCode());
    assertEquals("Method not found", error.getMessage());
    assertTrue(error.getData().isPresent());
    assertEquals(data, error.getData().get());
  }

  // Test invalidParams factory methods

  @Test
  public void testInvalidParams_NoData() {
    JsonRpcError<?> error = StandardErrors.invalidParams();

    assertEquals(-32602, error.getCode());
    assertEquals("Invalid params", error.getMessage());
    assertFalse(error.getData().isPresent());
  }

  @Test
  public void testInvalidParams_WithData() {
    String data = "Expected 2 parameters, got 1";
    JsonRpcError<String> error = StandardErrors.invalidParams(data);

    assertEquals(-32602, error.getCode());
    assertEquals("Invalid params", error.getMessage());
    assertTrue(error.getData().isPresent());
    assertEquals(data, error.getData().get());
  }

  // Test internalError factory methods

  @Test
  public void testInternalError_NoData() {
    JsonRpcError<?> error = StandardErrors.internalError();

    assertEquals(-32603, error.getCode());
    assertEquals("Internal error", error.getMessage());
    assertFalse(error.getData().isPresent());
  }

  @Test
  public void testInternalError_WithData() {
    String data = "Database connection failed";
    JsonRpcError<String> error = StandardErrors.internalError(data);

    assertEquals(-32603, error.getCode());
    assertEquals("Internal error", error.getMessage());
    assertTrue(error.getData().isPresent());
    assertEquals(data, error.getData().get());
  }

  // Test serverError factory methods

  @Test
  public void testServerError_ValidCode_NoData() {
    JsonRpcError<?> error = StandardErrors.serverError(-32000, "Custom server error");

    assertEquals(-32000, error.getCode());
    assertEquals("Custom server error", error.getMessage());
    assertFalse(error.getData().isPresent());
  }

  @Test
  public void testServerError_ValidCode_WithData() {
    String data = "Additional context";
    JsonRpcError<String> error = StandardErrors.serverError(-32050, "Server busy", data);

    assertEquals(-32050, error.getCode());
    assertEquals("Server busy", error.getMessage());
    assertTrue(error.getData().isPresent());
    assertEquals(data, error.getData().get());
  }

  @Test
  public void testServerError_MinBoundary() {
    JsonRpcError<?> error = StandardErrors.serverError(-32099, "Min boundary");
    assertEquals(-32099, error.getCode());
  }

  @Test
  public void testServerError_MaxBoundary() {
    JsonRpcError<?> error = StandardErrors.serverError(-32000, "Max boundary");
    assertEquals(-32000, error.getCode());
  }

  @Test
  public void testServerError_InvalidCode_TooLow() {
    JsonRpcException exception =
        assertThrows(JsonRpcException.class, () -> StandardErrors.serverError(-32100, "Invalid"));
    assertEquals(-32603, exception.getError().getCode());
    assertTrue(exception.getError().getMessage().contains("must be in range"));
  }

  @Test
  public void testServerError_InvalidCode_TooHigh() {
    JsonRpcException exception =
        assertThrows(JsonRpcException.class, () -> StandardErrors.serverError(-31999, "Invalid"));
    assertEquals(-32603, exception.getError().getCode());
  }

  @Test
  public void testServerError_InvalidCode_Positive() {
    JsonRpcException exception =
        assertThrows(JsonRpcException.class, () -> StandardErrors.serverError(100, "Invalid"));
    assertEquals(-32603, exception.getError().getCode());
  }

  // Test isReservedErrorCode

  @Test
  public void testIsReservedErrorCode_StandardErrors() {
    assertTrue(StandardErrors.isReservedErrorCode(-32700));
    assertTrue(StandardErrors.isReservedErrorCode(-32600));
    assertTrue(StandardErrors.isReservedErrorCode(-32601));
    assertTrue(StandardErrors.isReservedErrorCode(-32602));
    assertTrue(StandardErrors.isReservedErrorCode(-32603));
  }

  @Test
  public void testIsReservedErrorCode_ServerErrors() {
    assertTrue(StandardErrors.isReservedErrorCode(-32000));
    assertTrue(StandardErrors.isReservedErrorCode(-32050));
    assertTrue(StandardErrors.isReservedErrorCode(-32099));
  }

  @Test
  public void testIsReservedErrorCode_Boundaries() {
    assertTrue(StandardErrors.isReservedErrorCode(-32768));
    assertTrue(StandardErrors.isReservedErrorCode(-32000));
    assertFalse(StandardErrors.isReservedErrorCode(-32769));
    assertFalse(StandardErrors.isReservedErrorCode(-31999));
  }

  @Test
  public void testIsReservedErrorCode_NonReserved() {
    assertFalse(StandardErrors.isReservedErrorCode(0));
    assertFalse(StandardErrors.isReservedErrorCode(100));
    assertFalse(StandardErrors.isReservedErrorCode(-100));
    assertFalse(StandardErrors.isReservedErrorCode(-31999));
  }

  // Test isServerErrorCode

  @Test
  public void testIsServerErrorCode_ValidRange() {
    assertTrue(StandardErrors.isServerErrorCode(-32000));
    assertTrue(StandardErrors.isServerErrorCode(-32050));
    assertTrue(StandardErrors.isServerErrorCode(-32099));
  }

  @Test
  public void testIsServerErrorCode_Boundaries() {
    assertTrue(StandardErrors.isServerErrorCode(-32099));
    assertTrue(StandardErrors.isServerErrorCode(-32000));
    assertFalse(StandardErrors.isServerErrorCode(-32100));
    assertFalse(StandardErrors.isServerErrorCode(-31999));
  }

  @Test
  public void testIsServerErrorCode_StandardErrors() {
    assertFalse(StandardErrors.isServerErrorCode(-32700));
    assertFalse(StandardErrors.isServerErrorCode(-32600));
    assertFalse(StandardErrors.isServerErrorCode(-32601));
    assertFalse(StandardErrors.isServerErrorCode(-32602));
    assertFalse(StandardErrors.isServerErrorCode(-32603));
  }

  @Test
  public void testIsServerErrorCode_NonServerErrors() {
    assertFalse(StandardErrors.isServerErrorCode(0));
    assertFalse(StandardErrors.isServerErrorCode(100));
    assertFalse(StandardErrors.isServerErrorCode(-100));
  }

  // Test utility class cannot be instantiated

  @Test
  public void testCannotInstantiate() {
    assertThrows(
        java.lang.reflect.InvocationTargetException.class,
        () -> {
          java.lang.reflect.Constructor<?> constructor =
              StandardErrors.class.getDeclaredConstructor();
          constructor.setAccessible(true);
          constructor.newInstance();
        });
  }
}
