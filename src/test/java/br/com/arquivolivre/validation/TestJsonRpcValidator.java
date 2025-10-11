package br.com.arquivolivre.validation;

import static org.junit.jupiter.api.Assertions.*;

import br.com.arquivolivre.JsonRpcException;
import org.junit.jupiter.api.Test;

/** Comprehensive unit tests for JsonRpcValidator. */
public class TestJsonRpcValidator {

  // Method validation tests

  @Test
  public void testValidateMethod_ValidMethod() {
    assertDoesNotThrow(() -> JsonRpcValidator.validateMethod("subtract"));
    assertDoesNotThrow(() -> JsonRpcValidator.validateMethod("myMethod"));
    assertDoesNotThrow(() -> JsonRpcValidator.validateMethod("method_with_underscore"));
  }

  @Test
  public void testValidateMethod_NullMethod() {
    JsonRpcException exception =
        assertThrows(JsonRpcException.class, () -> JsonRpcValidator.validateMethod(null));
    assertEquals(-32600, exception.getError().getCode());
    assertTrue(exception.getError().getMessage().contains("cannot be null or empty"));
  }

  @Test
  public void testValidateMethod_EmptyMethod() {
    JsonRpcException exception =
        assertThrows(JsonRpcException.class, () -> JsonRpcValidator.validateMethod(""));
    assertEquals(-32600, exception.getError().getCode());
  }

  @Test
  public void testValidateMethod_WhitespaceMethod() {
    JsonRpcException exception =
        assertThrows(JsonRpcException.class, () -> JsonRpcValidator.validateMethod("   "));
    assertEquals(-32600, exception.getError().getCode());
  }

  @Test
  public void testValidateMethod_ReservedRpcPrefix() {
    JsonRpcException exception =
        assertThrows(JsonRpcException.class, () -> JsonRpcValidator.validateMethod("rpc.internal"));
    assertEquals(-32600, exception.getError().getCode());
    assertTrue(exception.getError().getMessage().contains("reserved"));
  }

  @Test
  public void testValidateMethod_RpcPrefixExactly() {
    JsonRpcException exception =
        assertThrows(JsonRpcException.class, () -> JsonRpcValidator.validateMethod("rpc."));
    assertEquals(-32600, exception.getError().getCode());
  }

  @Test
  public void testValidateMethod_RpcNotAtStart() {
    // "rpc." only reserved at the start
    assertDoesNotThrow(() -> JsonRpcValidator.validateMethod("myrpc.method"));
  }

  // ID validation tests

  @Test
  public void testValidateId_NullId() {
    assertDoesNotThrow(() -> JsonRpcValidator.validateId(null));
  }

  @Test
  public void testValidateId_StringId() {
    assertDoesNotThrow(() -> JsonRpcValidator.validateId("string-id"));
    assertDoesNotThrow(() -> JsonRpcValidator.validateId(""));
  }

  @Test
  public void testValidateId_IntegerId() {
    assertDoesNotThrow(() -> JsonRpcValidator.validateId(1));
    assertDoesNotThrow(() -> JsonRpcValidator.validateId(0));
    assertDoesNotThrow(() -> JsonRpcValidator.validateId(-1));
  }

  @Test
  public void testValidateId_LongId() {
    assertDoesNotThrow(() -> JsonRpcValidator.validateId(123456789L));
  }

  @Test
  public void testValidateId_DoubleId() {
    assertDoesNotThrow(() -> JsonRpcValidator.validateId(42.0));
  }

  @Test
  public void testValidateId_BooleanId() {
    JsonRpcException exception =
        assertThrows(JsonRpcException.class, () -> JsonRpcValidator.validateId(true));
    assertEquals(-32600, exception.getError().getCode());
    assertTrue(exception.getError().getMessage().contains("String, Number, or null"));
  }

  @Test
  public void testValidateId_ObjectId() {
    JsonRpcException exception =
        assertThrows(JsonRpcException.class, () -> JsonRpcValidator.validateId(new Object()));
    assertEquals(-32600, exception.getError().getCode());
  }

  @Test
  public void testValidateId_ArrayId() {
    JsonRpcException exception =
        assertThrows(
            JsonRpcException.class, () -> JsonRpcValidator.validateId(new int[] {1, 2, 3}));
    assertEquals(-32600, exception.getError().getCode());
  }

  // Error code validation tests

  @Test
  public void testValidateErrorCode_ValidCodes() {
    assertDoesNotThrow(() -> JsonRpcValidator.validateErrorCode(-32700));
    assertDoesNotThrow(() -> JsonRpcValidator.validateErrorCode(-32600));
    assertDoesNotThrow(() -> JsonRpcValidator.validateErrorCode(0));
    assertDoesNotThrow(() -> JsonRpcValidator.validateErrorCode(100));
    assertDoesNotThrow(() -> JsonRpcValidator.validateErrorCode(-100));
  }

  // Message validation tests

  @Test
  public void testValidateMessage_ValidMessage() {
    assertDoesNotThrow(() -> JsonRpcValidator.validateMessage("Valid error message"));
    assertDoesNotThrow(() -> JsonRpcValidator.validateMessage("Single sentence."));
  }

  @Test
  public void testValidateMessage_NullMessage() {
    JsonRpcException exception =
        assertThrows(JsonRpcException.class, () -> JsonRpcValidator.validateMessage(null));
    assertEquals(-32603, exception.getError().getCode());
    assertTrue(exception.getError().getMessage().contains("cannot be null or empty"));
  }

  @Test
  public void testValidateMessage_EmptyMessage() {
    JsonRpcException exception =
        assertThrows(JsonRpcException.class, () -> JsonRpcValidator.validateMessage(""));
    assertEquals(-32603, exception.getError().getCode());
  }

  @Test
  public void testValidateMessage_WhitespaceMessage() {
    JsonRpcException exception =
        assertThrows(JsonRpcException.class, () -> JsonRpcValidator.validateMessage("   "));
    assertEquals(-32603, exception.getError().getCode());
  }

  // JSON-RPC version validation tests

  @Test
  public void testValidateJsonRpcVersion_Valid() {
    assertDoesNotThrow(() -> JsonRpcValidator.validateJsonRpcVersion("2.0"));
  }

  @Test
  public void testValidateJsonRpcVersion_Invalid() {
    JsonRpcException exception =
        assertThrows(JsonRpcException.class, () -> JsonRpcValidator.validateJsonRpcVersion("1.0"));
    assertEquals(-32600, exception.getError().getCode());
    assertTrue(exception.getError().getMessage().contains("must be '2.0'"));
  }

  @Test
  public void testValidateJsonRpcVersion_Null() {
    JsonRpcException exception =
        assertThrows(JsonRpcException.class, () -> JsonRpcValidator.validateJsonRpcVersion(null));
    assertEquals(-32600, exception.getError().getCode());
  }

  @Test
  public void testValidateJsonRpcVersion_Empty() {
    JsonRpcException exception =
        assertThrows(JsonRpcException.class, () -> JsonRpcValidator.validateJsonRpcVersion(""));
    assertEquals(-32600, exception.getError().getCode());
  }
}
