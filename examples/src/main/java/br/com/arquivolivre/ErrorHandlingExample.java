package br.com.arquivolivre;

import br.com.arquivolivre.jsonrpc.JsonRpcRequest;
import br.com.arquivolivre.jsonrpc.JsonRpcResponse;
import br.com.arquivolivre.jsonrpc.JsonRpcError;
import br.com.arquivolivre.jsonrpc.StandardErrors;

/**
 * Example demonstrating error handling with standard and custom errors.
 */
public class ErrorHandlingExample {
    
    record ValidationError(String field, String expected, String actual) {}
    record UserParams(String userId) {}
    record UserResult(String userId, String name, String email) {}
    
    public static void main(String[] args) {
        System.out.println("=== Error Handling Example ===\n");
        
        // Standard errors
        System.out.println("Standard Errors:");
        printError("Parse Error", StandardErrors.parseError());
        printError("Invalid Request", StandardErrors.invalidRequest());
        printError("Method Not Found", StandardErrors.methodNotFound());
        printError("Invalid Params", StandardErrors.invalidParams());
        printError("Internal Error", StandardErrors.internalError());
        
        // Server error
        var serverError = StandardErrors.serverError(-32001, "Database connection failed");
        printError("Server Error", serverError);
        
        // Custom error with data
        var customError = JsonRpcError.of(
            -32602,
            "Invalid params",
            new ValidationError("age", "number", "string")
        );
        
        System.out.println("\nCustom Error with Data:");
        System.out.println("  Code: " + customError.getCode());
        System.out.println("  Message: " + customError.getMessage());
        System.out.println("  Data: " + customError.getData().get());
        
        // Simulate request handling
        System.out.println("\n--- Simulating Request Handling ---");
        
        var request = JsonRpcRequest.<UserParams>builder()
            .method("getUser")
            .params(new UserParams("123"))
            .id(1)
            .build();
        
        var response = handleRequest(request);
        
        System.out.println("\nRequest: " + request.getMethod());
        System.out.println("Response Success: " + response.isSuccess());
        if (response.isSuccess()) {
            System.out.println("Result: " + response.getResult().get());
        }
    }
    
    private static void printError(String name, JsonRpcError<?> error) {
        System.out.println("  " + name + ": " + error.getCode() + " - " + error.getMessage());
    }
    
    private static JsonRpcResponse<UserResult> handleRequest(JsonRpcRequest<UserParams> request) {
        try {
            var params = request.getParams().orElseThrow();
            var result = new UserResult(params.userId(), "John Doe", "john@example.com");
            return JsonRpcResponse.success(request.getId().orElse(null), result);
        } catch (Exception e) {
            return JsonRpcResponse.error(
                request.getId().orElse(null),
                StandardErrors.internalError()
            );
        }
    }
}
