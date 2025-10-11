[![CI](https://github.com/devops-thiago/json-rpc/actions/workflows/ci.yaml/badge.svg)](https://github.com/devops-thiago/json-rpc/actions/workflows/ci.yaml)
[![codecov](https://codecov.io/gh/devops-thiago/json-rpc/branch/main/graph/badge.svg)](https://codecov.io/gh/devops-thiago/json-rpc)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=devops-thiago_json-rpc&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=devops-thiago_json-rpc)
[![Maven Central](https://img.shields.io/maven-central/v/br.com.arquivolivre/json-rpc.svg)](https://central.sonatype.com/artifact/br.com.arquivolivre/json-rpc)

# JSON-RPC 2.0 Library

A lightweight, type-safe Java library for working with JSON-RPC 2.0 protocol. Built with immutability, validation, and ease of use in mind.

## Features

- ✅ Full JSON-RPC 2.0 specification compliance
- ✅ Immutable, thread-safe objects
- ✅ Type-safe generic support for params and results
- ✅ Built-in validation with clear error messages
- ✅ Fluent builder pattern API
- ✅ Standard error codes included
- ✅ Gson serialization support
- ✅ 88% test coverage

## Requirements

- Java 21 or higher
- Gson 2.10.1 (included as dependency)

## Installation

Add this dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>br.com.arquivolivre</groupId>
    <artifactId>json-rpc</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

### Creating Requests

```java
record SubtractParams(int minuend, int subtrahend) {}

var request = JsonRpcRequest.<SubtractParams>builder()
    .method("subtract")
    .params(new SubtractParams(42, 23))
    .id(1)
    .build();

// Without parameters
var request = JsonRpcRequest.builder()
    .method("getStatus")
    .id("req-123")
    .build();

// Notification (no response expected)
var notification = JsonRpcRequest.notification("update", new SubtractParams(5, 3));
```

### Creating Responses

```java
var response = JsonRpcResponse.success(1, 19);
var errorResponse = JsonRpcResponse.error(1, StandardErrors.methodNotFound());
```

### Working with Errors

```java
var error = StandardErrors.parseError();           // -32700
var error = StandardErrors.invalidRequest();       // -32600
var error = StandardErrors.methodNotFound();       // -32601
var error = StandardErrors.invalidParams();        // -32602
var error = StandardErrors.internalError();        // -32603
var error = StandardErrors.serverError(-32001, "Database failed");

// Custom error with data
record ValidationError(String field, String expected, String actual) {}
var error = JsonRpcError.of(-32602, "Invalid params", new ValidationError("age", "number", "string"));
```

### JSON Serialization

```java
var gson = JsonRpcGsonConfig.createGson();

record SubtractParams(int minuend, int subtrahend) {}

var request = JsonRpcRequest.<SubtractParams>builder()
    .method("subtract")
    .params(new SubtractParams(42, 23))
    .id(1)
    .build();

var json = gson.toJson(request);
// {"jsonrpc":"2.0","method":"subtract","params":{"minuend":42,"subtrahend":23},"id":1}

var responseJson = "{\"jsonrpc\":\"2.0\",\"result\":19,\"id\":1}";
var response = gson.fromJson(responseJson, new TypeToken<JsonRpcResponse<Integer>>(){}.getType());

System.out.println(response.getResult().get()); // 19
```

## API Reference

### JsonRpcRequest
- `builder()` - Create request with method, params, id
- `notification(method, params)` - Create notification (no id)
- `getMethod()`, `getParams()`, `getId()`, `isNotification()`

### JsonRpcResponse
- `success(id, result)` - Create success response
- `error(id, error)` - Create error response
- `getResult()`, `getError()`, `isSuccess()`, `isError()`

### JsonRpcError
- `of(code, message)` or `of(code, message, data)`
- `builder()` - Fluent builder pattern
- `getCode()`, `getMessage()`, `getData()`

### StandardErrors
- `parseError()`, `invalidRequest()`, `methodNotFound()`, `invalidParams()`, `internalError()`
- `serverError(code, message)` - Custom server errors (-32000 to -32099)

## Validation

Automatic validation for method names, IDs, error messages, and response structure. Throws `JsonRpcException` on validation failure.

```java
try {
    var request = JsonRpcRequest.builder().method("rpc.internal").id(1).build();
} catch (JsonRpcException e) {
    var error = e.getError();
    System.out.println(error.getCode());     // -32600
    System.out.println(error.getMessage());  // "Invalid Request: ..."
}
```

## Examples

### Complete Flow

```java
record SubtractParams(int minuend, int subtrahend) {}

var gson = JsonRpcGsonConfig.createGson();

// Client creates request
var request = JsonRpcRequest.<SubtractParams>builder()
    .method("subtract")
    .params(new SubtractParams(42, 23))
    .id(1)
    .build();

var requestJson = gson.toJson(request);

// Server processes and responds
var response = JsonRpcResponse.success(1, 19);
var responseJson = gson.toJson(response);

// Client handles response
if (response.isSuccess()) {
    System.out.println("Result: " + response.getResult().get());
}
```

### Error Handling

```java
record UserParams(String userId) {}
record UserResult(String userId, String name, String email) {}

public JsonRpcResponse<UserResult> handleRequest(JsonRpcRequest<UserParams> request) {
    try {
        var params = request.getParams().orElseThrow();
        var result = fetchUser(params.userId());
        return JsonRpcResponse.success(request.getId().orElse(null), result);
    } catch (MethodNotFoundException e) {
        return JsonRpcResponse.error(request.getId().orElse(null), StandardErrors.methodNotFound());
    } catch (InvalidParamsException e) {
        return JsonRpcResponse.error(request.getId().orElse(null), StandardErrors.invalidParams(e.getMessage()));
    } catch (Exception e) {
        return JsonRpcResponse.error(request.getId().orElse(null), StandardErrors.internalError());
    }
}
```

## Testing

```bash
mvn clean test                        # Run tests
open target/site/jacoco/index.html    # View coverage report
```

## CI/CD

The project uses GitHub Actions for continuous integration with the following checks:

- **Code Style**: Checkstyle with Google Java Style
- **Code Format**: Formatter validation
- **Static Analysis**: SpotBugs for bug detection
- **Tests**: JUnit 5 test suite
- **Coverage**: JaCoCo with 80% minimum threshold
- **Quality**: SonarCloud analysis
- **Reporting**: Codecov integration

## Contributing

Contributions welcome! Ensure tests pass and coverage stays above 80%.
