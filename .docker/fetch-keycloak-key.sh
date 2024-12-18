#!/bin/bash

# Configurações
KEYCLOAK_JWKS_URL="http://keycloak:8080/realms/recargapay_wallet_test/protocol/openid-connect/certs"
OUTPUT_DIR="/etc/kafka/certs"
OUTPUT_FILE="public.pem"

# Cria o diretório de saída, se necessário
mkdir -p "$OUTPUT_DIR"
echo "script 1"
# Obtém as chaves do Keycloak
echo "Buscando chave pública do Keycloak em $KEYCLOAK_JWKS_URL..."
jwks=$(curl -s $KEYCLOAK_JWKS_URL)

# Extraindo a chave pública (primeira chave no JSON)
modulus=$(echo "$jwks" | jq -r '.n')
exponent=$(echo "$jwks" | jq -r '.e')

# Converter URL-safe Base64 para Base64 padrão
modulus=$(echo "$modulus" | tr '-_' '+/')
exponent=$(echo "$exponent" | tr '-_' '+/')

# Adicionar padding ao Base64, se necessário
modulus=$(echo "$modulus" | sed -E 's/^(.*)$/\1====/' | cut -c1-$((${#modulus}+3-(${#modulus}%4))))
exponent=$(echo "$exponent" | sed -E 's/^(.*)$/\1====/' | cut -c1-$((${#exponent}+3-(${#exponent}%4))))

# Validar os valores extraídos
if [[ -z "$modulus" || -z "$exponent" ]]; then
  echo "Erro: Não foi possível extrair os valores de modulus (n) ou exponent (e)."
  exit 1
fi

echo "Modulus (convertido): $modulus"
echo "Exponent (convertido): $exponent"

# Decodificar os valores Base64
modulus_decoded=$(echo "$modulus" | base64 -d 2>/dev/null | xxd -p | tr -d '\n')
exponent_decoded=$(echo "$exponent" | base64 -d 2>/dev/null | xxd -p | tr -d '\n')

if [[ -z "$modulus_decoded" || -z "$exponent_decoded" ]]; then
  echo "Erro: Não foi possível decodificar os valores de modulus ou exponent."
  exit 1
fi

echo "Modulus (decodificado): $modulus_decoded"
echo "Exponent (decodificado): $exponent_decoded"

# Gerar o arquivo PEM
echo "Gerando arquivo PEM..."
openssl rsa -in <(echo "{\"n\":\"$modulus\",\"e\":\"$exponent\"}" | \
    jq -r '{kty:"RSA", n:.n, e:.e}' | \
    python3 -c "import sys, json; from Cryptodome.PublicKey import RSA; data = json.load(sys.stdin); print(RSA.construct((int(data['n'], 16), int(data['e'], 16))).export_key().decode())") \
    -pubin -out "$OUTPUT_DIR/$OUTPUT_FILE"

echo "Chave salva em $OUTPUT_DIR/$OUTPUT_FILE"
