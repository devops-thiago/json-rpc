package br.com.arquivolivre.gson;

import br.com.arquivolivre.JsonRpcException;
import br.com.arquivolivre.JsonRpcRequest;
import br.com.arquivolivre.StandardErrors;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

/** Gson TypeAdapter for JsonRpcRequest that handles generic params serialization. */
public class JsonRpcRequestAdapter extends TypeAdapter<JsonRpcRequest<?>> {
  private final Gson gson;

  /**
   * Constructs a JsonRpcRequestAdapter with the given Gson instance.
   *
   * @param gson the Gson instance to use for nested serialization
   */
  public JsonRpcRequestAdapter(Gson gson) {
    this.gson = gson;
  }

  @Override
  public void write(JsonWriter out, JsonRpcRequest<?> request) throws IOException {
    if (request == null) {
      out.nullValue();
      return;
    }

    out.beginObject();

    out.name("jsonrpc");
    out.value(request.getJsonrpc());

    out.name("method");
    out.value(request.getMethod());

    var paramsOptional = request.getParams();
    if (paramsOptional.isPresent()) {
      out.name("params");
      Object params = paramsOptional.get();
      gson.toJson(params, params.getClass(), out);
    }

    var idOptional = request.getId();
    if (idOptional.isPresent()) {
      out.name("id");
      var id = idOptional.get();
      // Use pattern matching for instanceof (Java 16+)
      switch (id) {
        case String s -> out.value(s);
        case Number n -> out.value(n);
        case null, default -> out.nullValue();
      }
    }

    out.endObject();
  }

  @Override
  public JsonRpcRequest<?> read(JsonReader in) throws IOException {
    JsonObject jsonObject;
    try {
      jsonObject = JsonParser.parseReader(in).getAsJsonObject();
    } catch (JsonSyntaxException e) {
      throw new JsonRpcException(StandardErrors.parseError("Invalid JSON"), e);
    }

    // Validate jsonrpc field
    if (!jsonObject.has("jsonrpc")) {
      throw new JsonRpcException(StandardErrors.invalidRequest("Missing 'jsonrpc' field"));
    }
    var jsonrpc = jsonObject.get("jsonrpc").getAsString();
    if (!"2.0".equals(jsonrpc)) {
      throw new JsonRpcException(
          StandardErrors.invalidRequest("Invalid 'jsonrpc' version: " + jsonrpc));
    }

    // Validate method field
    if (!jsonObject.has("method")) {
      throw new JsonRpcException(StandardErrors.invalidRequest("Missing 'method' field"));
    }
    var method = jsonObject.get("method").getAsString();

    // Build request
    var builder = JsonRpcRequest.<Object>builder();
    builder.method(method);

    // Handle params
    if (jsonObject.has("params")) {
      var paramsElement = jsonObject.get("params");
      builder.params(paramsElement);
    }

    // Handle id with pattern matching
    if (jsonObject.has("id")) {
      var idElement = jsonObject.get("id");
      if (idElement.isJsonNull()) {
        builder.id((Object) null);
      } else if (idElement instanceof JsonPrimitive primitive) {
        if (primitive.isString()) {
          builder.id(primitive.getAsString());
        } else if (primitive.isNumber()) {
          builder.id(primitive.getAsNumber());
        } else {
          throw new JsonRpcException(StandardErrors.invalidRequest("Invalid 'id' type"));
        }
      } else {
        throw new JsonRpcException(StandardErrors.invalidRequest("Invalid 'id' type"));
      }
    }

    return builder.build();
  }
}
