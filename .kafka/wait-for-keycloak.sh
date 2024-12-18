#!/bin/bash

KEYCLOAK_URL="http://keycloak:8080/realms/recargapay_wallet_test/protocol/openid-connect/certs"
RETRY_INTERVAL=5
MAX_RETRIES=30

echo "Aguardando o Keycloak ficar disponível em $KEYCLOAK_URL..."

for ((i=1; i<=MAX_RETRIES; i++)); do
  if curl -s --head --fail "$KEYCLOAK_URL" >/dev/null 2>&1; then
    echo "Keycloak está acessível!"
    exit 0
  fi
  echo "Tentativa $i/$MAX_RETRIES: Keycloak ainda não está disponível. Tentando novamente em $RETRY_INTERVAL segundos..."
  sleep $RETRY_INTERVAL
done

echo "Keycloak não ficou acessível após $((MAX_RETRIES * RETRY_INTERVAL)) segundos. Abortando."
exit 1
