package br.com.arquivolivre;

import br.com.arquivolivre.jsonrpc.JsonRpcRequest;
import br.com.arquivolivre.jsonrpc.JsonRpcResponse;
import br.com.arquivolivre.jsonrpc.JsonRpcError;
import br.com.arquivolivre.jsonrpc.StandardErrors;
import br.com.arquivolivre.jsonrpc.gson.JsonRpcGsonConfig;

/**
 * Basic example demonstrating JSON-RPC request and response creation.
 */
public class BasicExample {
    
    record SubtractParams(int minuend, int subtrahend) {}
    
    public static void main(String[] args) {
        System.out.println("=== Basic JSON-RPC Example ===\n");
        
        // Create a request
        var request = JsonRpcRequest.<SubtractParams>builder()
            .method("subtract")
            .params(new SubtractParams(42, 23))
            .id(1)
            .build();
        
        System.out.println("Request created:");
        System.out.println("  Method: " + request.getMethod());
        System.out.println("  Params: " + request.getParams().get());
        System.out.println("  ID: " + request.getId().get());
        System.out.println("  Is Notification: " + request.isNotification());
        
        // Create a success response
        var response = JsonRpcResponse.success(1, 19);
        
        System.out.println("\nSuccess Response created:");
        System.out.println("  Result: " + response.getResult().get());
        System.out.println("  ID: " + response.getId());
        System.out.println("  Is Success: " + response.isSuccess());
        
        // Create an error response
        var errorResponse = JsonRpcResponse.error(2, StandardErrors.methodNotFound());
        
        System.out.println("\nError Response created:");
        System.out.println("  Error Code: " + errorResponse.getError().get().getCode());
        System.out.println("  Error Message: " + errorResponse.getError().get().getMessage());
        System.out.println("  Is Error: " + errorResponse.isError());
    }
}
