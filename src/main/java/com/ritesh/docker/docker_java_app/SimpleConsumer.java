package com.ritesh.docker.docker_java_app;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.TableId;
import com.google.auth.oauth2.ServiceAccountCredentials;

import java.io.File;
import java.io.FileInputStream;
import java.time.Duration;
import java.util.*;

public class SimpleConsumer {

    public static void main(String[] args) {
        // Kafka consumer properties
        Properties props = new Properties();
        props.put("bootstrap.servers", "host.docker.internal:9092");
        props.put("group.id", "demo-group");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("test"));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                String message = record.value();
                System.out.println("Received message: " + message);

                insertIntoBigQuery(message);
            }
        }
    }

    public static void insertIntoBigQuery(String message) {
        try {
            // Get credentials path from environment variable
            String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
            if (credentialsPath == null || credentialsPath.isEmpty()) {
                throw new RuntimeException("GOOGLE_APPLICATION_CREDENTIALS not set");
            }

            System.out.println("GOOGLE_APPLICATION_CREDENTIALS=" + credentialsPath);

            File credFile = new File(credentialsPath);
            if (!credFile.exists() || !credFile.isFile()) {
                throw new RuntimeException("Credential file not found: " + credFile.getAbsolutePath());
            }

            // Load credentials
            try (FileInputStream serviceAccountStream = new FileInputStream(credFile)) {
                ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);

                BigQuery bigquery = BigQueryOptions.newBuilder()
                        .setCredentials(credentials)
                        .build()
                        .getService();

                // Target table
                TableId tableId = TableId.of("kafka_pipeline", "messages");

                // Row content
                Map<String, Object> rowContent = new HashMap<>();
                rowContent.put("id", UUID.randomUUID().toString());
                rowContent.put("message", message);
                rowContent.put("timestamp", System.currentTimeMillis() / 1000.0);

                // Insert request
                InsertAllRequest request = InsertAllRequest.newBuilder(tableId)
                        .addRow(rowContent)
                        .build();

                InsertAllResponse response = bigquery.insertAll(request);

                if (response.hasErrors()) {
                    System.out.println("Insert failed: " + response.getInsertErrors());
                } else {
                    System.out.println("Data inserted successfully: " + message);
                }
            }

        } catch (Exception e) {
            System.out.println("Exception while inserting into BigQuery: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
