FROM openjdk:17-alpine

# Define o diretório de trabalho
WORKDIR /opt

# Instala bash e curl para os scripts de espera
RUN apk add --no-cache bash curl

# Define variáveis de ambiente
ENV PORT=8084
ENV TZ="America/Sao_Paulo"

# Expõe a porta diretamente
EXPOSE 8084

# Copia o aplicativo e os scripts para o container
COPY target/app.jar /opt/app.jar
COPY wait-for-it.sh /opt/wait-for-it.sh
COPY wait-for-http.sh /opt/wait-for-http.sh 

# Garante que os scripts sejam executáveis
RUN chmod +x /opt/wait-for-it.sh
RUN chmod +x /opt/wait-for-http.sh

# Define o ponto de entrada
ENTRYPOINT ["/bin/bash", "-c", "/opt/wait-for-it.sh postgres:5432 -- /opt/wait-for-http.sh http://keycloak:8080/realms/recargapay_wallet_test/protocol/openid-connect/certs && exec java -jar app.jar"]
	