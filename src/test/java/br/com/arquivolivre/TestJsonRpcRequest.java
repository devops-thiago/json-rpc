package br.com.arquivolivre;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.HashMap;

/**
 * Comprehensive unit tests for JsonRpcRequest.
 */
public class TestJsonRpcRequest {
    
    // Builder pattern tests
    
    @Test
    public void testBuilder_AllFields() {
        JsonRpcRequest<String> request = JsonRpcRequest.<String>builder()
            .method("subtract")
            .params("test-params")
            .id(1)
            .build();
        
        assertEquals("2.0", request.getJsonrpc());
        assertEquals("subtract", request.getMethod());
        assertTrue(request.getParams().isPresent());
        assertEquals("test-params", request.getParams().get());
        assertTrue(request.getId().isPresent());
        assertEquals(1, request.getId().get());
        assertFalse(request.isNotification());
    }
    
    @Test
    public void testBuilder_WithoutParams() {
        JsonRpcRequest<?> request = JsonRpcRequest.builder()
            .method("getStatus")
            .id(1)
            .build();
        
        assertEquals("2.0", request.getJsonrpc());
        assertEquals("getStatus", request.getMethod());
        assertFalse(request.getParams().isPresent());
        assertTrue(request.getId().isPresent());
    }
    
    @Test
    public void testBuilder_WithoutId() {
        JsonRpcRequest<String> request = JsonRpcRequest.<String>builder()
            .method("notify")
            .params("notification-data")
            .build();
        
        assertEquals("notify", request.getMethod());
        assertTrue(request.getParams().isPresent());
        assertFalse(request.getId().isPresent());
        assertTrue(request.isNotification());
    }
    
    @Test
    public void testBuilder_StringId() {
        JsonRpcRequest<?> request = JsonRpcRequest.builder()
            .method("test")
            .id("req-123")
            .build();
        
        assertTrue(request.getId().isPresent());
        assertEquals("req-123", request.getId().get());
    }
    
    @Test
    public void testBuilder_NumberId() {
        JsonRpcRequest<?> request = JsonRpcRequest.builder()
            .method("test")
            .id(42)
            .build();
        
        assertTrue(request.getId().isPresent());
        assertEquals(42, request.getId().get());
    }
    
    @Test
    public void testBuilder_ObjectId() {
        JsonRpcRequest<?> request = JsonRpcRequest.builder()
            .method("test")
            .id((Object) "obj-id")
            .build();
        
        assertTrue(request.getId().isPresent());
        assertEquals("obj-id", request.getId().get());
    }
    
    @Test
    public void testBuilder_ComplexParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("subtrahend", 23);
        params.put("minuend", 42);
        
        JsonRpcRequest<Map<String, Object>> request = JsonRpcRequest.<Map<String, Object>>builder()
            .method("subtract")
            .params(params)
            .id(1)
            .build();
        
        assertTrue(request.getParams().isPresent());
        assertEquals(params, request.getParams().get());
    }
    
    @Test
    public void testBuilder_NullParams() {
        JsonRpcRequest<String> request = JsonRpcRequest.<String>builder()
            .method("test")
            .params(null)
            .id(1)
            .build();
        
        assertFalse(request.getParams().isPresent());
    }
    
    // Notification factory method tests
    
    @Test
    public void testNotification_WithParams() {
        JsonRpcRequest<String> notification = JsonRpcRequest.notification("update", "new-data");
        
        assertEquals("2.0", notification.getJsonrpc());
        assertEquals("update", notification.getMethod());
        assertTrue(notification.getParams().isPresent());
        assertEquals("new-data", notification.getParams().get());
        assertFalse(notification.getId().isPresent());
        assertTrue(notification.isNotification());
    }
    
    @Test
    public void testNotification_WithNullParams() {
        JsonRpcRequest<Object> notification = JsonRpcRequest.notification("ping", null);
        
        assertEquals("ping", notification.getMethod());
        assertFalse(notification.getParams().isPresent());
        assertTrue(notification.isNotification());
    }
    
    @Test
    public void testNotification_WithComplexParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("event", "user.login");
        params.put("userId", 123);
        
        JsonRpcRequest<Map<String, Object>> notification = JsonRpcRequest.notification("notify", params);
        
        assertTrue(notification.getParams().isPresent());
        assertEquals(params, notification.getParams().get());
        assertTrue(notification.isNotification());
    }
    
    // Method validation tests
    
    @Test
    public void testValidation_NullMethod() {
        JsonRpcException exception = assertThrows(JsonRpcException.class, 
            () -> JsonRpcRequest.builder()
                .method(null)
                .id(1)
                .build());
        assertNotNull(exception.getError());
        assertTrue(exception.getError().getMessage().contains("method name"));
    }
    
    @Test
    public void testValidation_EmptyMethod() {
        JsonRpcException exception = assertThrows(JsonRpcException.class, 
            () -> JsonRpcRequest.builder()
                .method("")
                .id(1)
                .build());
        assertNotNull(exception.getError());
    }
    
    @Test
    public void testValidation_WhitespaceMethod() {
        JsonRpcException exception = assertThrows(JsonRpcException.class, 
            () -> JsonRpcRequest.builder()
                .method("   ")
                .id(1)
                .build());
        assertNotNull(exception.getError());
    }
    
    @Test
    public void testValidation_RpcPrefixMethod() {
        JsonRpcException exception = assertThrows(JsonRpcException.class, 
            () -> JsonRpcRequest.builder()
                .method("rpc.test")
                .id(1)
                .build());
        assertNotNull(exception.getError());
        assertTrue(exception.getError().getMessage().contains("rpc."));
    }
    
    @Test
    public void testValidation_RpcDotPrefixMethod() {
        JsonRpcException exception = assertThrows(JsonRpcException.class, 
            () -> JsonRpcRequest.builder()
                .method("rpc.internal.method")
                .id(1)
                .build());
        assertNotNull(exception.getError());
    }
    
    // ID validation tests
    
    @Test
    public void testValidation_InvalidIdType() {
        JsonRpcException exception = assertThrows(JsonRpcException.class, 
            () -> JsonRpcRequest.builder()
                .method("test")
                .id(new Object())
                .build());
        assertNotNull(exception.getError());
        assertTrue(exception.getError().getMessage().contains("must be a String, Number, or null"));
    }
    
    @Test
    public void testValidation_ArrayId() {
        JsonRpcException exception = assertThrows(JsonRpcException.class, 
            () -> JsonRpcRequest.builder()
                .method("test")
                .id(new int[]{1, 2, 3})
                .build());
        assertNotNull(exception.getError());
    }
    
    @Test
    public void testValidation_MapId() {
        JsonRpcException exception = assertThrows(JsonRpcException.class, 
            () -> JsonRpcRequest.builder()
                .method("test")
                .id(new HashMap<>())
                .build());
        assertNotNull(exception.getError());
    }
    
    @Test
    public void testValidation_NullIdExplicit() {
        // Explicitly setting id to null should be valid (creates notification)
        JsonRpcRequest<?> request = JsonRpcRequest.builder()
            .method("test")
            .id((String) null)
            .build();
        
        assertFalse(request.getId().isPresent());
        assertTrue(request.isNotification());
    }
    
    // Edge case tests
    
    @Test
    public void testEdgeCase_ZeroId() {
        JsonRpcRequest<?> request = JsonRpcRequest.builder()
            .method("test")
            .id(0)
            .build();
        
        assertTrue(request.getId().isPresent());
        assertEquals(0, request.getId().get());
        assertFalse(request.isNotification());
    }
    
    @Test
    public void testEdgeCase_NegativeId() {
        JsonRpcRequest<?> request = JsonRpcRequest.builder()
            .method("test")
            .id(-1)
            .build();
        
        assertTrue(request.getId().isPresent());
        assertEquals(-1, request.getId().get());
    }
    
    @Test
    public void testEdgeCase_LargeNumberId() {
        JsonRpcRequest<?> request = JsonRpcRequest.builder()
            .method("test")
            .id(Long.MAX_VALUE)
            .build();
        
        assertTrue(request.getId().isPresent());
        assertEquals(Long.MAX_VALUE, request.getId().get());
    }
    
    @Test
    public void testEdgeCase_EmptyStringId() {
        JsonRpcRequest<?> request = JsonRpcRequest.builder()
            .method("test")
            .id("")
            .build();
        
        assertTrue(request.getId().isPresent());
        assertEquals("", request.getId().get());
    }
    
    @Test
    public void testEdgeCase_SpecialCharactersInMethod() {
        JsonRpcRequest<?> request = JsonRpcRequest.builder()
            .method("user.login")
            .id(1)
            .build();
        
        assertEquals("user.login", request.getMethod());
    }
    
    @Test
    public void testEdgeCase_UnderscoreInMethod() {
        JsonRpcRequest<?> request = JsonRpcRequest.builder()
            .method("get_user_data")
            .id(1)
            .build();
        
        assertEquals("get_user_data", request.getMethod());
    }
    
    @Test
    public void testEdgeCase_NumbersInMethod() {
        JsonRpcRequest<?> request = JsonRpcRequest.builder()
            .method("method123")
            .id(1)
            .build();
        
        assertEquals("method123", request.getMethod());
    }
    
    @Test
    public void testEdgeCase_LongMethodName() {
        String longMethod = "thisIsAVeryLongMethodNameThatShouldStillBeValidAccordingToTheSpecification";
        JsonRpcRequest<?> request = JsonRpcRequest.builder()
            .method(longMethod)
            .id(1)
            .build();
        
        assertEquals(longMethod, request.getMethod());
    }
    
    // isNotification tests
    
    @Test
    public void testIsNotification_WithId() {
        JsonRpcRequest<?> request = JsonRpcRequest.builder()
            .method("test")
            .id(1)
            .build();
        
        assertFalse(request.isNotification());
    }
    
    @Test
    public void testIsNotification_WithoutId() {
        JsonRpcRequest<?> request = JsonRpcRequest.builder()
            .method("test")
            .build();
        
        assertTrue(request.isNotification());
    }
    
    @Test
    public void testIsNotification_FactoryMethod() {
        JsonRpcRequest<String> notification = JsonRpcRequest.notification("test", "data");
        assertTrue(notification.isNotification());
    }
    
    // Jsonrpc version tests
    
    @Test
    public void testJsonrpcVersion_AlwaysTwoDotZero() {
        JsonRpcRequest<?> request1 = JsonRpcRequest.builder()
            .method("test1")
            .id(1)
            .build();
        
        JsonRpcRequest<?> request2 = JsonRpcRequest.notification("test2", null);
        
        assertEquals("2.0", request1.getJsonrpc());
        assertEquals("2.0", request2.getJsonrpc());
    }
    
    // Params type tests
    
    @Test
    public void testParams_StringType() {
        JsonRpcRequest<String> request = JsonRpcRequest.<String>builder()
            .method("test")
            .params("string-param")
            .id(1)
            .build();
        
        assertTrue(request.getParams().isPresent());
        assertEquals("string-param", request.getParams().get());
    }
    
    @Test
    public void testParams_IntegerType() {
        JsonRpcRequest<Integer> request = JsonRpcRequest.<Integer>builder()
            .method("test")
            .params(42)
            .id(1)
            .build();
        
        assertTrue(request.getParams().isPresent());
        assertEquals(42, request.getParams().get());
    }
    
    @Test
    public void testParams_ListType() {
        java.util.List<Integer> params = java.util.Arrays.asList(42, 23);
        JsonRpcRequest<java.util.List<Integer>> request = JsonRpcRequest.<java.util.List<Integer>>builder()
            .method("subtract")
            .params(params)
            .id(1)
            .build();
        
        assertTrue(request.getParams().isPresent());
        assertEquals(params, request.getParams().get());
    }
    
    @Test
    public void testParams_MapType() {
        Map<String, Object> params = new HashMap<>();
        params.put("key1", "value1");
        params.put("key2", 123);
        
        JsonRpcRequest<Map<String, Object>> request = JsonRpcRequest.<Map<String, Object>>builder()
            .method("test")
            .params(params)
            .id(1)
            .build();
        
        assertTrue(request.getParams().isPresent());
        assertEquals(params, request.getParams().get());
    }
}
