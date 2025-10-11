package br.com.arquivolivre;

import java.util.Optional;

/**
 * Represents an immutable JSON-RPC 2.0 response object.
 *
 * <p>A response object contains:
 *
 * <ul>
 *   <li>jsonrpc: Always "2.0"
 *   <li>id: The request id (can be null for parse errors)
 *   <li>result: The result of a successful call (mutually exclusive with error)
 *   <li>error: The error object for a failed call (mutually exclusive with result)
 * </ul>
 *
 * <p>This class enforces that exactly one of result or error is present. Use the factory methods
 * success() or error() to create instances.
 *
 * @param <T> the type of the result field
 */
public final class JsonRpcResponse<T> {
  private final String jsonrpc;
  private final Object id;
  private final T result;
  private final JsonRpcError<?> error;
  private final boolean isSuccess;

  /**
   * Private constructor that enforces mutual exclusivity of result and error. Use factory methods
   * success() or error() instead.
   */
  private JsonRpcResponse(Object id, T result, JsonRpcError<?> error, boolean isSuccess) {
    // Validate mutual exclusivity
    if (isSuccess && error != null) {
      throw new JsonRpcException(
          JsonRpcError.of(-32603, "Internal Error: response cannot have both result and error"));
    }
    if (!isSuccess && result != null) {
      throw new JsonRpcException(
          JsonRpcError.of(-32603, "Internal Error: response cannot have both result and error"));
    }
    if (!isSuccess && error == null) {
      throw new JsonRpcException(
          JsonRpcError.of(-32603, "Internal Error: error response must have an error object"));
    }

    this.jsonrpc = "2.0";
    this.id = id;
    this.result = result;
    this.error = error;
    this.isSuccess = isSuccess;
  }

  /**
   * Creates a success response with the given id and result.
   *
   * @param id the request id (can be null)
   * @param result the result value
   * @param <T> the type of the result
   * @return a new success JsonRpcResponse instance
   */
  public static <T> JsonRpcResponse<T> success(Object id, T result) {
    return new JsonRpcResponse<>(id, result, null, true);
  }

  /**
   * Creates an error response with the given id and error.
   *
   * @param id the request id (can be null for parse errors)
   * @param error the error object
   * @param <T> the type of the result (not used in error responses)
   * @return a new error JsonRpcResponse instance
   */
  public static <T> JsonRpcResponse<T> error(Object id, JsonRpcError<?> error) {
    if (error == null) {
      throw new JsonRpcException(
          JsonRpcError.of(-32603, "Internal Error: error object cannot be null"));
    }
    return new JsonRpcResponse<>(id, null, error, false);
  }

  /**
   * Gets the JSON-RPC version (always "2.0").
   *
   * @return the JSON-RPC version
   */
  public String getJsonrpc() {
    return jsonrpc;
  }

  /**
   * Gets the request id.
   *
   * @return the request id (can be null)
   */
  public Object getId() {
    return id;
  }

  /**
   * Gets the result field if this is a success response.
   *
   * @return an Optional containing the result, or empty if this is an error response
   */
  public Optional<T> getResult() {
    return Optional.ofNullable(result);
  }

  /**
   * Gets the error field if this is an error response.
   *
   * @return an Optional containing the error, or empty if this is a success response
   */
  public Optional<JsonRpcError<?>> getError() {
    return Optional.ofNullable(error);
  }

  /**
   * Checks if this is a success response.
   *
   * @return true if this response contains a result, false if it contains an error
   */
  public boolean isSuccess() {
    return isSuccess;
  }

  /**
   * Checks if this is an error response.
   *
   * @return true if this response contains an error, false if it contains a result
   */
  public boolean isError() {
    return !isSuccess;
  }

  @Override
  public String toString() {
    if (isSuccess) {
      return "JsonRpcResponse{"
          + "jsonrpc='"
          + jsonrpc
          + '\''
          + ", id="
          + id
          + ", result="
          + result
          + '}';
    } else {
      return "JsonRpcResponse{"
          + "jsonrpc='"
          + jsonrpc
          + '\''
          + ", id="
          + id
          + ", error="
          + error
          + '}';
    }
  }
}
