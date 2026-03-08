package com.ritesh.docker.docker_java_app;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.TableId;
import com.google.auth.oauth2.ServiceAccountCredentials;
import java.io.FileInputStream;

import java.time.Duration;
import java.util.*;

public class SimpleConsumer {

    public static void main(String[] args) {
        // Kafka consumer properties
        Properties props = new Properties();
        //props.put("bootstrap.servers", "localhost:9092");
        props.put("bootstrap.servers", "host.docker.internal:9092");
        props.put("group.id", "demo-group");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());

        // Create consumer and subscribe to topic "test"
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("test"));

        // Poll loop
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                String message = record.value();
                System.out.println("Received message: " + message);

                // Insert into BigQuery
                insertIntoBigQuery(message);
            }
        }
    }

    // BigQuery insert function
    public static void insertIntoBigQuery(String message) {
        try {
            BigQuery bigquery = BigQueryOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(
                    //new FileInputStream("C:\\Users\\Ritesh Mishra\\eclipse-workspace\\docker-java-app\\src\\main\\resources\\kafka-pipeline-project-136237569977.json")))
                		new FileInputStream(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"))))
                .build()
                .getService();

            TableId tableId = TableId.of("kafka_pipeline", "messages");

            Map<String, Object> rowContent = new HashMap<>();
            rowContent.put("id", UUID.randomUUID().toString());
            rowContent.put("message", message);
            rowContent.put("timestamp", System.currentTimeMillis() / 1000.0); // ISO format for TIMESTAMP

            InsertAllRequest request = InsertAllRequest.newBuilder(tableId)
                .addRow(rowContent)
                .build();

            InsertAllResponse response = bigquery.insertAll(request);

            if (response.hasErrors()) {
                System.out.println("Insert failed: " + response.getInsertErrors());
            } else {
                System.out.println("Data inserted successfully: " + message);
            }
        } catch (Exception e) {
            System.out.println("Exception while inserting into BigQuery: " + e.getMessage());
            e.printStackTrace();
        }
    }

}