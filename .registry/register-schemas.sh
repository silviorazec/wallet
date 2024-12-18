#!/bin/bash

until curl -s http://schema-registry:8081/subjects; do
  echo "Aguardando Schema Registry iniciar..."
  sleep 5
done


TRANSACTION_SCHEMA='{
  "schema": "{\"namespace\":\"com.recargapay.code.assessment.topics\",\"type\":\"record\",\"name\":\"Transaction\",\"fields\":[{\"name\":\"amount\",\"type\":{\"type\":\"bytes\",\"logicalType\":\"decimal\",\"precision\":20,\"scale\":2}},{\"name\":\"relatedWalletId\",\"type\":[\"null\",{\"type\":\"string\",\"logicalType\":\"uuid\"}],\"default\":null},{\"name\":\"createdAt\",\"type\":{\"type\":\"long\",\"logicalType\":\"timestamp-millis\"}},{\"name\":\"typeTransaction\",\"type\":{\"type\":\"string\"}}]}"
}'

NOTIFICATION_SCHEMA='{
	"schema":"{\"type\":\"record\",\"name\":\"Notification\",\"namespace\":\"com.recargapay.code.assessment.topics\",\"fields\":[{\"name\":\"message\",\"type\":\"string\"},{\"name\":\"to\",\"type\":\"string\"}]}"
}'


curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
     --data "$TRANSACTION_SCHEMA" \
     http://schema-registry:8081/subjects/transaction/versions

curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
     --data "$NOTIFICATION_SCHEMA" \
     http://schema-registry:8081/subjects/notication/versions

echo "Schemas ok"
