#!/bin/bash


until kafka-topics --bootstrap-server kafka:9092 --list; do
  echo "Waint starting Kafka..."
  sleep 5
done


kafka-topics --create --topic transaction --bootstrap-server kafka:9092 --partitions 3 --replication-factor 1

kafka-topics --create --topic notification --bootstrap-server kafka:9092 --partitions 3 --replication-factor 1

echo "Topics created."
