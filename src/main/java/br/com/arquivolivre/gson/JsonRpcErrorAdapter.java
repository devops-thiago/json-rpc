package br.com.arquivolivre.gson;

import br.com.arquivolivre.JsonRpcError;
import br.com.arquivolivre.JsonRpcException;
import br.com.arquivolivre.StandardErrors;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

/** Gson TypeAdapter for JsonRpcError that handles optional data field. */
public class JsonRpcErrorAdapter extends TypeAdapter<JsonRpcError<?>> {
  private final Gson gson;

  /**
   * Constructs a JsonRpcErrorAdapter with the given Gson instance.
   *
   * @param gson the Gson instance to use for nested serialization
   */
  public JsonRpcErrorAdapter(Gson gson) {
    this.gson = gson;
  }

  @Override
  public void write(JsonWriter out, JsonRpcError<?> error) throws IOException {
    if (error == null) {
      out.nullValue();
      return;
    }

    out.beginObject();

    out.name("code");
    out.value(error.getCode());

    out.name("message");
    out.value(error.getMessage());

    var dataOptional = error.getData();
    if (dataOptional.isPresent()) {
      out.name("data");
      Object data = dataOptional.get();
      gson.toJson(data, data.getClass(), out);
    }

    out.endObject();
  }

  @Override
  public JsonRpcError<?> read(JsonReader in) throws IOException {
    JsonObject jsonObject;
    try {
      jsonObject = JsonParser.parseReader(in).getAsJsonObject();
    } catch (JsonSyntaxException e) {
      throw new JsonRpcException(StandardErrors.parseError("Invalid JSON"), e);
    }

    // Validate code field
    if (!jsonObject.has("code")) {
      throw new JsonRpcException(
          StandardErrors.invalidRequest("Missing 'code' field in error object"));
    }
    if (!jsonObject.get("code").isJsonPrimitive()
        || !jsonObject.get("code").getAsJsonPrimitive().isNumber()) {
      throw new JsonRpcException(
          StandardErrors.invalidRequest("Invalid 'code' field: must be a number"));
    }
    var code = jsonObject.get("code").getAsInt();

    // Validate message field
    if (!jsonObject.has("message")) {
      throw new JsonRpcException(
          StandardErrors.invalidRequest("Missing 'message' field in error object"));
    }
    if (!jsonObject.get("message").isJsonPrimitive()
        || !jsonObject.get("message").getAsJsonPrimitive().isString()) {
      throw new JsonRpcException(
          StandardErrors.invalidRequest("Invalid 'message' field: must be a string"));
    }
    var message = jsonObject.get("message").getAsString();

    // Handle optional data field
    if (jsonObject.has("data")) {
      var data = jsonObject.get("data");
      return JsonRpcError.of(code, message, data);
    } else {
      return JsonRpcError.of(code, message);
    }
  }
}
