package br.com.arquivolivre.validation;

import br.com.arquivolivre.JsonRpcError;
import br.com.arquivolivre.JsonRpcException;

/**
 * Centralized validation logic for JSON-RPC objects. Provides static utility methods for validating
 * JSON-RPC 2.0 protocol constraints.
 */
public final class JsonRpcValidator {

  private JsonRpcValidator() {
    // Utility class, prevent instantiation
  }

  /**
   * Validates a JSON-RPC method name.
   *
   * @param method the method name to validate
   * @throws JsonRpcException if the method name is invalid
   */
  public static void validateMethod(String method) {
    if (method == null || method.trim().isEmpty()) {
      throw new JsonRpcException(
          JsonRpcError.of(-32600, "Invalid Request: method name cannot be null or empty"));
    }

    if (method.startsWith("rpc.")) {
      throw new JsonRpcException(
          JsonRpcError.of(
              -32600, "Invalid Request: method names starting with 'rpc.' are reserved"));
    }
  }

  /**
   * Validates a JSON-RPC request/response id.
   *
   * @param id the id to validate (can be String, Number, or null)
   * @throws JsonRpcException if the id type is invalid
   */
  public static void validateId(Object id) {
    if (id == null) {
      return; // null is valid
    }

    // Use pattern matching for instanceof (Java 16+)
    if (id instanceof Number numId) {
      // Warn if Number has fractional parts
      var doubleValue = numId.doubleValue();
      if (Math.abs(doubleValue - Math.floor(doubleValue)) > 1e-10) {
        System.err.println(
            "Warning: JSON-RPC id contains fractional parts, which is discouraged: " + id);
      }
    } else if (!(id instanceof String)) {
      throw new JsonRpcException(
          JsonRpcError.of(-32600, "Invalid Request: id must be a String, Number, or null"));
    }
  }

  /**
   * Validates a JSON-RPC error code.
   *
   * @param code the error code to validate
   * @throws JsonRpcException if the error code is invalid
   */
  public static void validateErrorCode(int code) {
    // Error codes are integers, so any int value is technically valid
    // This method exists for consistency and future validation rules
  }

  /**
   * Validates a JSON-RPC error message.
   *
   * @param message the error message to validate
   * @throws JsonRpcException if the message is invalid
   */
  public static void validateMessage(String message) {
    if (message == null || message.trim().isEmpty()) {
      throw new JsonRpcException(
          JsonRpcError.of(-32603, "Internal Error: error message cannot be null or empty"));
    }

    // Warn if message contains multiple sentences
    var trimmed = message.trim();
    var sentenceCount = trimmed.chars().filter(ch -> ch == '.' || ch == '!' || ch == '?').count();
    if (sentenceCount > 1) {
      System.err.println("Warning: Error message should be a single sentence: " + message);
    }
  }

  /**
   * Validates the JSON-RPC version string.
   *
   * @param version the version string to validate
   * @throws JsonRpcException if the version is not "2.0"
   */
  public static void validateJsonRpcVersion(String version) {
    if (!"2.0".equals(version)) {
      throw new JsonRpcException(
          JsonRpcError.of(-32600, "Invalid Request: jsonrpc version must be '2.0'"));
    }
  }
}
