#!/bin/bash


ENV_FILE=".docker/.env"


if [[ ! -f "$ENV_FILE" ]]; then
  echo "Erro: The file not found: $ENV_FILE"
  exit 1
fi


source $ENV_FILE



echo "Building Docker image: ${IMAGE_NAME}:${IMAGE_VERSION}"

echo "Executando Maven build..."
./mvnw clean package


docker build -f  api-wallet/Dockerfile -t ${API_IMAGE_NAME}:${WORKER_IMAGE_VERSION} api-wallet/

docker build -f  worker-wallet/Dockerfile -t ${WORKER_IMAGE_NAME}:${WORKER_IMAGE_VERSION} worker-wallet/



echo "call services"
docker-compose -f .docker/docker-compose.yml up -d
