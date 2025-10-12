package br.com.arquivolivre;

import br.com.arquivolivre.jsonrpc.JsonRpcRequest;
import br.com.arquivolivre.jsonrpc.JsonRpcResponse;
import br.com.arquivolivre.jsonrpc.gson.JsonRpcGsonConfig;
import com.google.gson.reflect.TypeToken;

/**
 * Complete example showing a full request-response flow with JSON serialization.
 */
public class CompleteFlowExample {
    
    record SubtractParams(int minuend, int subtrahend) {}
    
    public static void main(String[] args) {
        System.out.println("=== Complete Request-Response Flow ===\n");
        
        var gson = JsonRpcGsonConfig.createGson();
        
        // CLIENT SIDE
        System.out.println("--- CLIENT SIDE ---");
        
        // Client creates request
        var request = JsonRpcRequest.<SubtractParams>builder()
            .method("subtract")
            .params(new SubtractParams(42, 23))
            .id(1)
            .build();
        
        var requestJson = gson.toJson(request);
        System.out.println("Client sends request:");
        System.out.println(requestJson);
        
        // SERVER SIDE
        System.out.println("\n--- SERVER SIDE ---");
        
        // Server receives and deserializes request
        JsonRpcRequest<SubtractParams> receivedRequest = gson.fromJson(requestJson, new TypeToken<JsonRpcRequest<SubtractParams>>(){}.getType());
        System.out.println("Server received request:");
        System.out.println("  Method: " + receivedRequest.getMethod());
        System.out.println("  Params: " + receivedRequest.getParams().get());
        
        // Server processes request
        var params = receivedRequest.getParams().get();
        var result = params.minuend() - params.subtrahend();
        System.out.println("  Calculated result: " + result);
        
        // Server creates response
        var response = JsonRpcResponse.success(receivedRequest.getId().orElse(null), result);
        var responseJson = gson.toJson(response);
        System.out.println("Server sends response:");
        System.out.println(responseJson);
        
        // CLIENT SIDE
        System.out.println("\n--- CLIENT SIDE ---");
        
        // Client receives and deserializes response
        JsonRpcResponse<Integer> receivedResponse = gson.fromJson(responseJson, new TypeToken<JsonRpcResponse<Integer>>(){}.getType());
        System.out.println("Client received response:");
        
        if (receivedResponse.isSuccess()) {
            System.out.println("  Success! Result: " + receivedResponse.getResult().get());
        } else {
            System.out.println("  Error: " + receivedResponse.getError().get().getMessage());
        }
        
        // NOTIFICATION EXAMPLE
        System.out.println("\n--- NOTIFICATION (No Response Expected) ---");
        
        var notification = JsonRpcRequest.notification("logEvent", new SubtractParams(5, 3));
        var notificationJson = gson.toJson(notification);
        
        System.out.println("Client sends notification:");
        System.out.println(notificationJson);
        System.out.println("  Is Notification: " + notification.isNotification());
        System.out.println("  Has ID: " + notification.getId().isPresent());
    }
}
