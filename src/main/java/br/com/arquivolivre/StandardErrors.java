package br.com.arquivolivre;

/**
 * Utility class providing predefined standard JSON-RPC 2.0 error codes and factory methods.
 * 
 * <p>This class defines constants for all standard JSON-RPC 2.0 errors and provides
 * convenient factory methods for creating error objects. It also includes validation
 * methods for reserved error code ranges.
 * 
 * <p>Standard error codes:
 * <ul>
 *   <li>-32700: Parse error - Invalid JSON was received</li>
 *   <li>-32600: Invalid Request - The JSON sent is not a valid Request object</li>
 *   <li>-32601: Method not found - The method does not exist or is not available</li>
 *   <li>-32602: Invalid params - Invalid method parameter(s)</li>
 *   <li>-32603: Internal error - Internal JSON-RPC error</li>
 * </ul>
 * 
 * <p>Reserved error code ranges:
 * <ul>
 *   <li>-32768 to -32000: Reserved for predefined errors</li>
 *   <li>-32000 to -32099: Reserved for server-specific errors</li>
 * </ul>
 */
public final class StandardErrors {
    
    // Standard error codes
    public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;
    
    // Error code ranges
    public static final int SERVER_ERROR_MIN = -32099;
    public static final int SERVER_ERROR_MAX = -32000;
    public static final int RESERVED_ERROR_MIN = -32768;
    public static final int RESERVED_ERROR_MAX = -32000;
    
    // Standard error messages
    private static final String PARSE_ERROR_MSG = "Parse error";
    private static final String INVALID_REQUEST_MSG = "Invalid Request";
    private static final String METHOD_NOT_FOUND_MSG = "Method not found";
    private static final String INVALID_PARAMS_MSG = "Invalid params";
    private static final String INTERNAL_ERROR_MSG = "Internal error";
    
    /**
     * Private constructor to prevent instantiation.
     */
    private StandardErrors() {
        throw new AssertionError("StandardErrors is a utility class and should not be instantiated");
    }
    
    /**
     * Creates a Parse Error (-32700).
     * Invalid JSON was received by the server.
     * 
     * @return a JsonRpcError for parse error
     */
    public static JsonRpcError<?> parseError() {
        return JsonRpcError.of(PARSE_ERROR, PARSE_ERROR_MSG);
    }
    
    /**
     * Creates a Parse Error (-32700) with custom data.
     * 
     * @param data additional error information
     * @param <T> the type of the data field
     * @return a JsonRpcError for parse error with data
     */
    public static <T> JsonRpcError<T> parseError(T data) {
        return JsonRpcError.of(PARSE_ERROR, PARSE_ERROR_MSG, data);
    }
    
    /**
     * Creates an Invalid Request error (-32600).
     * The JSON sent is not a valid Request object.
     * 
     * @return a JsonRpcError for invalid request
     */
    public static JsonRpcError<?> invalidRequest() {
        return JsonRpcError.of(INVALID_REQUEST, INVALID_REQUEST_MSG);
    }
    
    /**
     * Creates an Invalid Request error (-32600) with custom data.
     * 
     * @param data additional error information
     * @param <T> the type of the data field
     * @return a JsonRpcError for invalid request with data
     */
    public static <T> JsonRpcError<T> invalidRequest(T data) {
        return JsonRpcError.of(INVALID_REQUEST, INVALID_REQUEST_MSG, data);
    }
    
    /**
     * Creates a Method Not Found error (-32601).
     * The method does not exist or is not available.
     * 
     * @return a JsonRpcError for method not found
     */
    public static JsonRpcError<?> methodNotFound() {
        return JsonRpcError.of(METHOD_NOT_FOUND, METHOD_NOT_FOUND_MSG);
    }
    
    /**
     * Creates a Method Not Found error (-32601) with custom data.
     * 
     * @param data additional error information
     * @param <T> the type of the data field
     * @return a JsonRpcError for method not found with data
     */
    public static <T> JsonRpcError<T> methodNotFound(T data) {
        return JsonRpcError.of(METHOD_NOT_FOUND, METHOD_NOT_FOUND_MSG, data);
    }
    
    /**
     * Creates an Invalid Params error (-32602).
     * Invalid method parameter(s).
     * 
     * @return a JsonRpcError for invalid params
     */
    public static JsonRpcError<?> invalidParams() {
        return JsonRpcError.of(INVALID_PARAMS, INVALID_PARAMS_MSG);
    }
    
    /**
     * Creates an Invalid Params error (-32602) with custom data.
     * 
     * @param data additional error information
     * @param <T> the type of the data field
     * @return a JsonRpcError for invalid params with data
     */
    public static <T> JsonRpcError<T> invalidParams(T data) {
        return JsonRpcError.of(INVALID_PARAMS, INVALID_PARAMS_MSG, data);
    }
    
    /**
     * Creates an Internal Error (-32603).
     * Internal JSON-RPC error.
     * 
     * @return a JsonRpcError for internal error
     */
    public static JsonRpcError<?> internalError() {
        return JsonRpcError.of(INTERNAL_ERROR, INTERNAL_ERROR_MSG);
    }
    
    /**
     * Creates an Internal Error (-32603) with custom data.
     * 
     * @param data additional error information
     * @param <T> the type of the data field
     * @return a JsonRpcError for internal error with data
     */
    public static <T> JsonRpcError<T> internalError(T data) {
        return JsonRpcError.of(INTERNAL_ERROR, INTERNAL_ERROR_MSG, data);
    }
    
    /**
     * Creates a custom server error in the range -32000 to -32099.
     * This range is reserved for implementation-defined server errors.
     * 
     * @param code the error code (must be in range -32099 to -32000)
     * @param message the error message
     * @return a JsonRpcError for server error
     * @throws JsonRpcException if the code is not in the valid server error range
     */
    public static JsonRpcError<?> serverError(int code, String message) {
        if (!isServerErrorCode(code)) {
            throw new JsonRpcException(
                JsonRpcError.of(INTERNAL_ERROR, 
                    "Server error code must be in range " + SERVER_ERROR_MIN + " to " + SERVER_ERROR_MAX)
            );
        }
        return JsonRpcError.of(code, message);
    }
    
    /**
     * Creates a custom server error in the range -32000 to -32099 with custom data.
     * 
     * @param code the error code (must be in range -32099 to -32000)
     * @param message the error message
     * @param data additional error information
     * @param <T> the type of the data field
     * @return a JsonRpcError for server error with data
     * @throws JsonRpcException if the code is not in the valid server error range
     */
    public static <T> JsonRpcError<T> serverError(int code, String message, T data) {
        if (!isServerErrorCode(code)) {
            throw new JsonRpcException(
                JsonRpcError.of(INTERNAL_ERROR, 
                    "Server error code must be in range " + SERVER_ERROR_MIN + " to " + SERVER_ERROR_MAX)
            );
        }
        return JsonRpcError.of(code, message, data);
    }
    
    /**
     * Checks if an error code is in the reserved range (-32768 to -32000).
     * 
     * @param code the error code to check
     * @return true if the code is reserved, false otherwise
     */
    public static boolean isReservedErrorCode(int code) {
        return code >= RESERVED_ERROR_MIN && code <= RESERVED_ERROR_MAX;
    }
    
    /**
     * Checks if an error code is in the server error range (-32099 to -32000).
     * 
     * @param code the error code to check
     * @return true if the code is in the server error range, false otherwise
     */
    public static boolean isServerErrorCode(int code) {
        return code >= SERVER_ERROR_MIN && code <= SERVER_ERROR_MAX;
    }
}
