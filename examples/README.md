# JSON-RPC Library Examples

Practical examples demonstrating the JSON-RPC 2.0 library usage.

## Prerequisites

The main library must be installed to your local Maven repository first.

From the project root directory:
```bash
mvn clean install
```

Then compile the examples:
```bash
cd examples
mvn clean compile
```

## Running Examples

### 1. BasicExample
Basic request and response creation.

```bash
mvn exec:java -Dexec.mainClass="br.com.arquivolivre.BasicExample"
```

### 2. SerializationExample
JSON serialization and deserialization.

```bash
mvn exec:java -Dexec.mainClass="br.com.arquivolivre.SerializationExample"
```

### 3. ErrorHandlingExample
Error handling with standard and custom errors.

```bash
mvn exec:java -Dexec.mainClass="br.com.arquivolivre.ErrorHandlingExample"
```

### 4. CompleteFlowExample
Complete client-server request-response flow.

```bash
mvn exec:java -Dexec.mainClass="br.com.arquivolivre.CompleteFlowExample"
```

### 5. ValidationExample
Validation rules and exception handling.

```bash
mvn exec:java -Dexec.mainClass="br.com.arquivolivre.ValidationExample"
```

## Run All Examples

```bash
./run-all.sh
```

## Requirements

- Java 21+
- Maven 3.6+
