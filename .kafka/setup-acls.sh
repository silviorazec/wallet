#!/bin/bash

/wait-for-it.sh zookeeper:2181 --timeout=60 -- echo "Zookeeper está disponível."


kafka-acls.sh --authorizer-properties zookeeper.connect=zookeeper:2181 \
  --add --allow-principal "User:KAFKA_TRANSACTION_PRODUCER" \
  --producer --topic transaction


kafka-acls.sh --authorizer-properties zookeeper.connect=zookeeper:2181 \
  --add --allow-principal "User:KAFKA_TRANSACTION_CONSUMER" \
  --consumer --topic transaction --group worker-wallet-consumer


