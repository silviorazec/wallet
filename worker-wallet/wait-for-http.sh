#!/bin/bash

# URL a ser verificada
URL=$1
TIMEOUT=180 # Timeout fixo de 3 minutos
ATTEMPTS=0

# Verificar se a URL foi passada
if [ -z "$URL" ]; then
  echo "Uso: $0 <url>"
  exit 1
fi

echo "Esperando pelo endpoint: $URL por até $((TIMEOUT / 60)) minutos..."

# Loop para tentar o URL até que esteja disponível ou atinja o timeout
while [ "$ATTEMPTS" -lt "$TIMEOUT" ]; do
  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$URL")
  
  # Verifica se o código HTTP é 200 (OK)
  if [ "$HTTP_CODE" -eq 200 ]; then
    echo "Serviço disponível no $URL (HTTP $HTTP_CODE)"
    exit 0
  fi

  # Incrementa a tentativa e espera
  ATTEMPTS=$((ATTEMPTS + 1))
  echo "Tentativa $ATTEMPTS/$TIMEOUT: Aguardando $URL... (HTTP $HTTP_CODE)"
  sleep 1
done

echo "Timeout atingido: $URL indisponível após $((TIMEOUT / 60)) minutos"
exit 1
