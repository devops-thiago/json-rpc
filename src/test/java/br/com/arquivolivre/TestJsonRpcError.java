package br.com.arquivolivre;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.HashMap;

/**
 * Comprehensive unit tests for JsonRpcError.
 */
public class TestJsonRpcError {
    
    // Factory method tests
    
    @Test
    public void testFactoryMethod_CodeAndMessage() {
        JsonRpcError<?> error = JsonRpcError.of(-32600, "Invalid Request");
        
        assertEquals(-32600, error.getCode());
        assertEquals("Invalid Request", error.getMessage());
        assertFalse(error.getData().isPresent());
    }
    
    @Test
    public void testFactoryMethod_CodeMessageAndData() {
        JsonRpcError<String> error = JsonRpcError.of(-32601, "Method not found", "subtract");
        
        assertEquals(-32601, error.getCode());
        assertEquals("Method not found", error.getMessage());
        assertTrue(error.getData().isPresent());
        assertEquals("subtract", error.getData().get());
    }
    
    @Test
    public void testFactoryMethod_WithComplexData() {
        Map<String, Object> data = new HashMap<>();
        data.put("expected", 2);
        data.put("actual", 1);
        
        JsonRpcError<Map<String, Object>> error = JsonRpcError.of(-32602, "Invalid params", data);
        
        assertEquals(-32602, error.getCode());
        assertTrue(error.getData().isPresent());
        assertEquals(data, error.getData().get());
    }
    
    // Builder pattern tests
    
    @Test
    public void testBuilder_AllFields() {
        JsonRpcError<Integer> error = JsonRpcError.<Integer>builder()
            .code(-32602)
            .message("Invalid params")
            .data(42)
            .build();
        
        assertEquals(-32602, error.getCode());
        assertEquals("Invalid params", error.getMessage());
        assertTrue(error.getData().isPresent());
        assertEquals(42, error.getData().get());
    }
    
    @Test
    public void testBuilder_WithoutData() {
        JsonRpcError<?> error = JsonRpcError.builder()
            .code(-32603)
            .message("Internal error")
            .build();
        
        assertEquals(-32603, error.getCode());
        assertEquals("Internal error", error.getMessage());
        assertFalse(error.getData().isPresent());
    }
    
    @Test
    public void testBuilder_WithNullData() {
        JsonRpcError<String> error = JsonRpcError.<String>builder()
            .code(-32603)
            .message("Internal error")
            .data(null)
            .build();
        
        assertFalse(error.getData().isPresent());
    }
    
    // Validation tests
    
    @Test
    public void testValidation_NullMessage() {
        JsonRpcException exception = assertThrows(JsonRpcException.class, 
            () -> JsonRpcError.of(-32600, null));
        assertEquals(-32603, exception.getError().getCode());
        assertTrue(exception.getError().getMessage().contains("cannot be null or empty"));
    }
    
    @Test
    public void testValidation_EmptyMessage() {
        JsonRpcException exception = assertThrows(JsonRpcException.class, 
            () -> JsonRpcError.of(-32600, ""));
        assertEquals(-32603, exception.getError().getCode());
    }
    
    @Test
    public void testValidation_WhitespaceMessage() {
        JsonRpcException exception = assertThrows(JsonRpcException.class, 
            () -> JsonRpcError.of(-32600, "   "));
        assertEquals(-32603, exception.getError().getCode());
    }
    
    @Test
    public void testBuilder_MissingCode() {
        JsonRpcException exception = assertThrows(JsonRpcException.class, 
            () -> JsonRpcError.builder()
                .message("Test message")
                .build());
        assertNotNull(exception.getError());
    }
    
    @Test
    public void testBuilder_MissingMessage() {
        JsonRpcException exception = assertThrows(JsonRpcException.class, 
            () -> JsonRpcError.builder()
                .code(-32600)
                .build());
        assertNotNull(exception.getError());
    }
    
    // Edge case tests
    
    @Test
    public void testError_WithZeroCode() {
        JsonRpcError<?> error = JsonRpcError.of(0, "Custom error");
        assertEquals(0, error.getCode());
    }
    
    @Test
    public void testError_WithPositiveCode() {
        JsonRpcError<?> error = JsonRpcError.of(100, "Application error");
        assertEquals(100, error.getCode());
    }
    
    @Test
    public void testError_WithLargeNegativeCode() {
        JsonRpcError<?> error = JsonRpcError.of(-99999, "Custom error");
        assertEquals(-99999, error.getCode());
    }
    
    @Test
    public void testError_MessageWithSpecialCharacters() {
        String message = "Error: \"special\" chars & symbols!";
        JsonRpcError<?> error = JsonRpcError.of(-32600, message);
        assertEquals(message, error.getMessage());
    }
    
    @Test
    public void testError_LongMessage() {
        String longMessage = "This is a very long error message that contains a lot of text to test how the system handles lengthy descriptions";
        JsonRpcError<?> error = JsonRpcError.of(-32600, longMessage);
        assertEquals(longMessage, error.getMessage());
    }
}
