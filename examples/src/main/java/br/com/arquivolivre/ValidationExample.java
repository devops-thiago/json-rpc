package br.com.arquivolivre;

/**
 * Example demonstrating validation and exception handling.
 */
public class ValidationExample {
    
    public static void main(String[] args) {
        System.out.println("=== Validation Example ===\n");
        
        // Valid request
        System.out.println("1. Creating valid request:");
        try {
            var request = JsonRpcRequest.builder()
                .method("validMethod")
                .id(1)
                .build();
            System.out.println("   ✓ Success: " + request.getMethod());
        } catch (JsonRpcException e) {
            System.out.println("   ✗ Error: " + e.getError().getMessage());
        }
        
        // Invalid: reserved method name
        System.out.println("\n2. Attempting to use reserved 'rpc.' prefix:");
        try {
            var request = JsonRpcRequest.builder()
                .method("rpc.internal")
                .id(1)
                .build();
            System.out.println("   ✓ Success");
        } catch (JsonRpcException e) {
            System.out.println("   ✗ Error: " + e.getError().getMessage());
            System.out.println("   Error Code: " + e.getError().getCode());
        }
        
        // Invalid: null method
        System.out.println("\n3. Attempting to use null method:");
        try {
            var request = JsonRpcRequest.builder()
                .method(null)
                .id(1)
                .build();
            System.out.println("   ✓ Success");
        } catch (JsonRpcException e) {
            System.out.println("   ✗ Error: " + e.getError().getMessage());
        }
        
        // Invalid: empty method
        System.out.println("\n4. Attempting to use empty method:");
        try {
            var request = JsonRpcRequest.builder()
                .method("")
                .id(1)
                .build();
            System.out.println("   ✓ Success");
        } catch (JsonRpcException e) {
            System.out.println("   ✗ Error: " + e.getError().getMessage());
        }
        
        // Invalid: wrong ID type
        System.out.println("\n5. Attempting to use invalid ID type (Object):");
        try {
            var request = JsonRpcRequest.builder()
                .method("test")
                .id(new Object())
                .build();
            System.out.println("   ✓ Success");
        } catch (JsonRpcException e) {
            System.out.println("   ✗ Error: " + e.getError().getMessage());
        }
        
        // Valid: String ID
        System.out.println("\n6. Creating request with String ID:");
        try {
            var request = JsonRpcRequest.builder()
                .method("test")
                .id("request-123")
                .build();
            System.out.println("   ✓ Success: ID = " + request.getId().get());
        } catch (JsonRpcException e) {
            System.out.println("   ✗ Error: " + e.getError().getMessage());
        }
        
        // Valid: Number ID
        System.out.println("\n7. Creating request with Number ID:");
        try {
            var request = JsonRpcRequest.builder()
                .method("test")
                .id(42)
                .build();
            System.out.println("   ✓ Success: ID = " + request.getId().get());
        } catch (JsonRpcException e) {
            System.out.println("   ✗ Error: " + e.getError().getMessage());
        }
        
        // Valid: Notification (no ID)
        System.out.println("\n8. Creating notification (no ID):");
        try {
            var notification = JsonRpcRequest.notification("notify", "data");
            System.out.println("   ✓ Success: Is Notification = " + notification.isNotification());
        } catch (JsonRpcException e) {
            System.out.println("   ✗ Error: " + e.getError().getMessage());
        }
        
        // Error validation
        System.out.println("\n9. Creating error with null message:");
        try {
            var error = JsonRpcError.of(-32600, null);
            System.out.println("   ✓ Success");
        } catch (JsonRpcException e) {
            System.out.println("   ✗ Error: " + e.getError().getMessage());
        }
        
        System.out.println("\n=== Validation Complete ===");
    }
}
