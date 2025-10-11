package br.com.arquivolivre.gson;

import br.com.arquivolivre.JsonRpcError;
import br.com.arquivolivre.JsonRpcRequest;
import br.com.arquivolivre.JsonRpcResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Utility class for creating Gson instances configured for JSON-RPC 2.0 serialization.
 *
 * <p>This class provides a pre-configured Gson instance with custom type adapters for
 * JsonRpcRequest, JsonRpcResponse, and JsonRpcError that properly handle:
 *
 * <ul>
 *   <li>Generic params serialization in requests
 *   <li>Result/error mutual exclusivity in responses
 *   <li>Optional data field in errors
 *   <li>Validation of all required fields during deserialization
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * Gson gson = JsonRpcGsonConfig.createGson();
 *
 * // Serialize a request
 * JsonRpcRequest<Map<String, Object>> request = JsonRpcRequest.<Map<String, Object>>builder()
 *     .method("subtract").params(Map.of("minuend", 42, "subtrahend", 23)).id(1).build();
 * String json = gson.toJson(request);
 *
 * // Deserialize a response
 * String responseJson = "{\"jsonrpc\":\"2.0\",\"result\":19,\"id\":1}";
 * JsonRpcResponse<?> response = gson.fromJson(responseJson, JsonRpcResponse.class);
 * }</pre>
 */
public final class JsonRpcGsonConfig {

  private JsonRpcGsonConfig() {
    // Utility class, prevent instantiation
  }

  /**
   * Creates a Gson instance configured for JSON-RPC 2.0 serialization.
   *
   * <p>The returned Gson instance includes custom type adapters for:
   *
   * <ul>
   *   <li>JsonRpcRequest - handles generic params and validation
   *   <li>JsonRpcResponse - handles result/error mutual exclusivity
   *   <li>JsonRpcError - handles optional data field
   * </ul>
   *
   * <p>All adapters perform validation during deserialization and throw JsonRpcException for
   * invalid JSON-RPC structures.
   *
   * @return a configured Gson instance
   */
  public static Gson createGson() {
    var builder = new GsonBuilder();

    // Create a base Gson for use by adapters
    var baseGson = builder.create();

    // Register type adapters
    builder.registerTypeAdapter(JsonRpcRequest.class, new JsonRpcRequestAdapter(baseGson));
    builder.registerTypeAdapter(JsonRpcResponse.class, new JsonRpcResponseAdapter(baseGson));
    builder.registerTypeAdapter(JsonRpcError.class, new JsonRpcErrorAdapter(baseGson));

    return builder.create();
  }
}
