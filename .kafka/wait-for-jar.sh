#!/bin/bash

JAR_NAME="custon-handler-oauth.jar"  
JAR_PATH="/opt/kafka/libs/$JAR_NAME"

echo "Esperando pelo JAR: $JAR_PATH"

while [ ! -f "$JAR_PATH" ]; do
    echo "JAR não encontrado, aguardando..."
    sleep 5
done

echo "JAR encontrado: $JAR_PATH"
exec "$@"
