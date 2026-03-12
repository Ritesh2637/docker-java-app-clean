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
            String credentialsDirPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
            if (credentialsDirPath == null || credentialsDirPath.isEmpty()) {
                throw new RuntimeException("GOOGLE_APPLICATION_CREDENTIALS not set");
            }

            // Debug print
            System.out.println("GOOGLE_APPLICATION_CREDENTIALS=" + credentialsDirPath);

            File credDir = new File(credentialsDirPath);
            if (!credDir.exists() || !credDir.isDirectory()) {
                throw new RuntimeException("Credential path is not a directory: " + credentialsDirPath);
            }

            File[] files = credDir.listFiles();
            if (files == null || files.length == 0) {
                throw new RuntimeException("No credential file found in: " + credentialsDirPath);
            }

            // Pick the first file inside the directory
            File credFile = files[0];
            System.out.println("Using credential file: " + credFile.getAbsolutePath());

            try (FileInputStream serviceAccountStream = new FileInputStream(credFile)) {
                ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);

                BigQuery bigquery = BigQueryOptions.newBuilder()
                        .setCredentials(credentials)
                        .build()
                        .getService();

                TableId tableId = TableId.of("kafka_pipeline", "messages");

                Map<String, Object> rowContent = new HashMap<>();
                rowContent.put("id", UUID.randomUUID().toString());
                rowContent.put("message", message);
                rowContent.put("timestamp", System.currentTimeMillis() / 1000.0);

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
