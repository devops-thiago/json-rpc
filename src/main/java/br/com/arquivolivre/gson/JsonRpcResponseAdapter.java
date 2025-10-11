package br.com.arquivolivre.gson;

import br.com.arquivolivre.JsonRpcError;
import br.com.arquivolivre.JsonRpcException;
import br.com.arquivolivre.JsonRpcResponse;
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

/** Gson TypeAdapter for JsonRpcResponse that handles result/error mutual exclusivity. */
public class JsonRpcResponseAdapter extends TypeAdapter<JsonRpcResponse<?>> {
  private final Gson gson;

  /**
   * Constructs a JsonRpcResponseAdapter with the given Gson instance.
   *
   * @param gson the Gson instance to use for nested serialization
   */
  public JsonRpcResponseAdapter(Gson gson) {
    this.gson = gson;
  }

  @Override
  public void write(JsonWriter out, JsonRpcResponse<?> response) throws IOException {
    if (response == null) {
      out.nullValue();
      return;
    }

    out.beginObject();

    out.name("jsonrpc");
    out.value(response.getJsonrpc());

    if (response.isSuccess()) {
      out.name("result");
      var resultOptional = response.getResult();
      if (resultOptional.isPresent()) {
        Object result = resultOptional.get();
        gson.toJson(result, result.getClass(), out);
      } else {
        out.nullValue();
      }
    } else {
      out.name("error");
      var errorOptional = response.getError();
      if (errorOptional.isPresent()) {
        gson.toJson(errorOptional.get(), JsonRpcError.class, out);
      }
    }

    out.name("id");
    var id = response.getId();
    // Use pattern matching with switch (Java 21)
    switch (id) {
      case String s -> out.value(s);
      case Number n -> out.value(n);
      case null, default -> out.nullValue();
    }

    out.endObject();
  }

  @Override
  public JsonRpcResponse<?> read(JsonReader in) throws IOException {
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

    // Validate id field
    if (!jsonObject.has("id")) {
      throw new JsonRpcException(StandardErrors.invalidRequest("Missing 'id' field"));
    }

    Object id = null;
    var idElement = jsonObject.get("id");
    if (!idElement.isJsonNull() && idElement instanceof JsonPrimitive primitive) {
      if (primitive.isString()) {
        id = primitive.getAsString();
      } else if (primitive.isNumber()) {
        id = primitive.getAsNumber();
      }
    }

    // Check for result or error
    var hasResult = jsonObject.has("result");
    var hasError = jsonObject.has("error");

    // Validate mutual exclusivity
    if (hasResult && hasError) {
      throw new JsonRpcException(
          StandardErrors.invalidRequest("Response cannot have both 'result' and 'error'"));
    }
    if (!hasResult && !hasError) {
      throw new JsonRpcException(
          StandardErrors.invalidRequest("Response must have either 'result' or 'error'"));
    }

    if (hasResult) {
      var result = jsonObject.get("result");
      return JsonRpcResponse.success(id, result);
    } else {
      var errorElement = jsonObject.get("error");
      var error = gson.fromJson(errorElement, JsonRpcError.class);
      return JsonRpcResponse.error(id, error);
    }
  }
}
