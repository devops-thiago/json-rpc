#!/bin/bash

echo "======================================"
echo "JSON-RPC Library Examples"
echo "======================================"
echo ""

examples=(
    "br.com.arquivolivre.BasicExample"
    "br.com.arquivolivre.SerializationExample"
    "br.com.arquivolivre.ErrorHandlingExample"
    "br.com.arquivolivre.CompleteFlowExample"
    "br.com.arquivolivre.ValidationExample"
)

for example in "${examples[@]}"; do
    echo ""
    echo "--------------------------------------"
    echo "Running: $example"
    echo "--------------------------------------"
    mvn -q exec:java -Dexec.mainClass="$example"
    echo ""
done

echo "======================================"
echo "All examples completed!"
echo "======================================"
