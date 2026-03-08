package com.ritesh.docker.docker_java_app;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import com.google.cloud.bigquery.FieldValueList;
import com.google.auth.oauth2.ServiceAccountCredentials;
import java.io.FileInputStream;

public class BigQueryInsertTest {
    public static void main(String[] args) throws Exception {
        // BigQuery client initialize with explicit project ID + credentials
        BigQuery bigquery = BigQueryOptions.newBuilder()
                .setProjectId("kafka-pipeline-project")   // apna actual GCP project ID
                .setCredentials(ServiceAccountCredentials.fromStream(
                    new FileInputStream("C:/Users/Ritesh Mishra/eclipse-workspace/docker-java-app/src/main/resources/kafka-pipeline-project-136237569977.json")
                ))
                .build()
                .getService();

        // Insert query
        String query = "INSERT INTO `kafka-pipeline-project.kafka_pipeline.messages` "
                     + "(id, message, timestamp) VALUES "
                     + "('1', 'Hello BigQuery from Java!', CURRENT_TIMESTAMP())";

        QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
        bigquery.query(queryConfig);

        System.out.println("Row inserted successfully!");
    }
}