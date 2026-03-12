public static void insertIntoBigQuery(String message) {
    try {
        String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (credentialsPath == null || credentialsPath.isEmpty()) {
            throw new RuntimeException("GOOGLE_APPLICATION_CREDENTIALS not set");
        }

        System.out.println("GOOGLE_APPLICATION_CREDENTIALS=" + credentialsPath);

        File credFile = new File(credentialsPath);
        if (!credFile.exists() || !credFile.isFile()) {
            throw new RuntimeException("Credential file not found: " + credentialsPath);
        }

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
