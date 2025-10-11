package br.com.arquivolivre;

/**
 * Runtime exception for JSON-RPC protocol violations. Carries a JsonRpcError for structured error
 * information.
 */
public class JsonRpcException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  private final transient JsonRpcError<?> error;

  /**
   * Creates a JsonRpcException with the given error.
   *
   * @param error the JSON-RPC error object
   */
  public JsonRpcException(JsonRpcError<?> error) {
    super(error.getMessage());
    this.error = error;
  }

  /**
   * Creates a JsonRpcException with the given error and cause.
   *
   * @param error the JSON-RPC error object
   * @param cause the underlying cause
   */
  public JsonRpcException(JsonRpcError<?> error, Throwable cause) {
    super(error.getMessage(), cause);
    this.error = error;
  }

  /**
   * Gets the JSON-RPC error object.
   *
   * @return the error object
   */
  public JsonRpcError<?> getError() {
    return error;
  }
}
