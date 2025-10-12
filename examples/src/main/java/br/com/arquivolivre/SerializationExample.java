package br.com.arquivolivre;

import br.com.arquivolivre.jsonrpc.JsonRpcRequest;
import br.com.arquivolivre.jsonrpc.JsonRpcResponse;
import br.com.arquivolivre.jsonrpc.gson.JsonRpcGsonConfig;
import com.google.gson.reflect.TypeToken;

/**
 * Example demonstrating JSON serialization and deserialization.
 */
public class SerializationExample {
    
    record SubtractParams(int minuend, int subtrahend) {}
    
    public static void main(String[] args) {
        System.out.println("=== JSON Serialization Example ===\n");
        
        var gson = JsonRpcGsonConfig.createGson();
        
        // Serialize a request
        var request = JsonRpcRequest.<SubtractParams>builder()
            .method("subtract")
            .params(new SubtractParams(42, 23))
            .id(1)
            .build();
        
        var requestJson = gson.toJson(request);
        System.out.println("Serialized Request:");
        System.out.println(requestJson);
        
        // Deserialize a response
        var responseJson = "{\"jsonrpc\":\"2.0\",\"result\":19,\"id\":1}";
        JsonRpcResponse<Integer> response = gson.fromJson(responseJson, new TypeToken<JsonRpcResponse<Integer>>(){}.getType());
        
        System.out.println("\nDeserialized Response:");
        System.out.println("  JSON: " + responseJson);
        System.out.println("  Result: " + response.getResult().get());
        System.out.println("  ID: " + response.getId());
        
        // Serialize a notification
        var notification = JsonRpcRequest.notification("update", new SubtractParams(5, 3));
        var notificationJson = gson.toJson(notification);
        
        System.out.println("\nSerialized Notification:");
        System.out.println(notificationJson);
        System.out.println("  Is Notification: " + notification.isNotification());
    }
}
