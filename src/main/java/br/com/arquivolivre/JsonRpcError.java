package br.com.arquivolivre;

import br.com.arquivolivre.validation.JsonRpcValidator;
import java.util.Optional;

/**
 * Represents an immutable JSON-RPC 2.0 error object.
 *
 * <p>An error object contains:
 *
 * <ul>
 *   <li>code: An integer error code
 *   <li>message: A concise string description (should be a single sentence)
 *   <li>data: Optional additional error information
 * </ul>
 *
 * <p>This record is immutable and thread-safe.
 *
 * @param <T> the type of the optional data field
 * @param code the error code
 * @param message the error message
 * @param data optional additional error information
 */
public record JsonRpcError<T>(int code, String message, T data) {

  /** Compact constructor with validation. */
  public JsonRpcError {
    JsonRpcValidator.validateErrorCode(code);
    JsonRpcValidator.validateMessage(message);
  }

  /**
   * Creates a simple error with code and message (no data).
   *
   * @param code the error code
   * @param message the error message
   * @param <T> the type of the optional data field
   * @return a new JsonRpcError instance
   */
  public static <T> JsonRpcError<T> of(int code, String message) {
    return new JsonRpcError<>(code, message, null);
  }

  /**
   * Creates an error with code, message, and data.
   *
   * @param code the error code
   * @param message the error message
   * @param data additional error information
   * @param <T> the type of the data field
   * @return a new JsonRpcError instance
   */
  public static <T> JsonRpcError<T> of(int code, String message, T data) {
    return new JsonRpcError<>(code, message, data);
  }

  /**
   * Gets the error code.
   *
   * @return the error code
   */
  public int getCode() {
    return code;
  }

  /**
   * Gets the error message.
   *
   * @return the error message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Gets the optional data field.
   *
   * @return an Optional containing the data, or empty if no data was provided
   */
  public Optional<T> getData() {
    return Optional.ofNullable(data);
  }

  /**
   * Creates a new builder for constructing a JsonRpcError.
   *
   * @param <T> the type of the optional data field
   * @return a new builder instance
   */
  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  /**
   * Builder for constructing JsonRpcError instances.
   *
   * @param <T> the type of the optional data field
   */
  public static final class Builder<T> {
    private Integer code;
    private String message;
    private T data;

    private Builder() {}

    /**
     * Sets the error code.
     *
     * @param code the error code
     * @return this builder
     */
    public Builder<T> code(int code) {
      this.code = code;
      return this;
    }

    /**
     * Sets the error message.
     *
     * @param message the error message
     * @return this builder
     */
    public Builder<T> message(String message) {
      this.message = message;
      return this;
    }

    /**
     * Sets the optional data field.
     *
     * @param data additional error information
     * @return this builder
     */
    public Builder<T> data(T data) {
      this.data = data;
      return this;
    }

    /**
     * Builds the JsonRpcError instance.
     *
     * @return a new JsonRpcError instance
     * @throws JsonRpcException if required fields are missing or invalid
     */
    public JsonRpcError<T> build() {
      if (code == null) {
        throw new JsonRpcException(
            JsonRpcError.of(-32603, "Internal Error: error code is required"));
      }
      if (message == null) {
        throw new JsonRpcException(
            JsonRpcError.of(-32603, "Internal Error: error message is required"));
      }

      return new JsonRpcError<>(code, message, data);
    }
  }
}
