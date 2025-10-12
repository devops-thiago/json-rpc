package br.com.arquivolivre.jsonrpc;

import br.com.arquivolivre.jsonrpc.validation.JsonRpcValidator;
import java.util.Optional;

/**
 * Immutable JSON-RPC 2.0 Request object. Represents a remote procedure call with method, optional
 * params, and optional id.
 *
 * @param <T> the type of the params field
 */
public final class JsonRpcRequest<T> {
  private final String jsonrpc;
  private final String method;
  private final T params;
  private final Object id;

  private JsonRpcRequest(Builder<T> builder) {
    this.jsonrpc = "2.0";
    this.method = builder.method;
    this.params = builder.params;
    this.id = builder.id;

    // Validate at construction time
    JsonRpcValidator.validateMethod(this.method);
    if (builder.hasId) {
      JsonRpcValidator.validateId(this.id);
    }
  }

  /**
   * Creates a new builder for constructing a JsonRpcRequest.
   *
   * @param <T> the type of the params field
   * @return a new builder instance
   */
  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  /**
   * Creates a notification (request without id).
   *
   * @param <T> the type of the params field
   * @param method the method name
   * @param params the parameters
   * @return a notification request
   */
  public static <T> JsonRpcRequest<T> notification(String method, T params) {
    return JsonRpcRequest.<T>builder().method(method).params(params).build();
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
   * Gets the method name.
   *
   * @return the method name
   */
  public String getMethod() {
    return method;
  }

  /**
   * Gets the params if present.
   *
   * @return an Optional containing the params, or empty if not set
   */
  public Optional<T> getParams() {
    return Optional.ofNullable(params);
  }

  /**
   * Gets the id if present.
   *
   * @return an Optional containing the id, or empty if this is a notification
   */
  public Optional<Object> getId() {
    return Optional.ofNullable(id);
  }

  /**
   * Checks if this is a notification (no id).
   *
   * @return true if this is a notification, false otherwise
   */
  public boolean isNotification() {
    return id == null;
  }

  /**
   * Builder for constructing JsonRpcRequest instances.
   *
   * @param <T> the type of the params field
   */
  public static class Builder<T> {
    private String method;
    private T params;
    private Object id;
    private boolean hasId = false;

    private Builder() {}

    /**
     * Sets the method name.
     *
     * @param method the method name
     * @return this builder
     */
    public Builder<T> method(String method) {
      this.method = method;
      return this;
    }

    /**
     * Sets the params.
     *
     * @param params the parameters
     * @return this builder
     */
    public Builder<T> params(T params) {
      this.params = params;
      return this;
    }

    /**
     * Sets the id as a String.
     *
     * @param id the id
     * @return this builder
     */
    public Builder<T> id(String id) {
      this.id = id;
      this.hasId = true;
      return this;
    }

    /**
     * Sets the id as a Number.
     *
     * @param id the id
     * @return this builder
     */
    public Builder<T> id(Number id) {
      this.id = id;
      this.hasId = true;
      return this;
    }

    /**
     * Sets the id (can be String, Number, or null).
     *
     * @param id the id
     * @return this builder
     */
    public Builder<T> id(Object id) {
      this.id = id;
      this.hasId = true;
      return this;
    }

    /**
     * Builds the JsonRpcRequest.
     *
     * @return the constructed JsonRpcRequest
     * @throws JsonRpcException if validation fails
     */
    public JsonRpcRequest<T> build() {
      return new JsonRpcRequest<>(this);
    }
  }
}
