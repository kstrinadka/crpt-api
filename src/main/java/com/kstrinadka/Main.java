package com.kstrinadka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());
    static CrptApi api = new CrptApi(TimeUnit.SECONDS, 5);

    public static void main(String[] args) throws IOException, InterruptedException {
        CrptApi.DocumentRequest documentRequest = getDocumentById(1);
        System.out.println(documentRequest);

        HttpResponse<String> response;
        try {
            response = api.createDocument(documentRequest);
            System.out.println("status: " + response.statusCode());
            System.out.println("body: " + response.body());
        } catch (CrptApi.TooManyRequestsException e) {
            log.log(Level.WARNING, e.getMessage());
        }
    }



    static final String DOCUMENTS_FOLDER_PATH_TEMPLATE = "/document%d.json";
    static final URL DOCUMENTS_FOLDER_URL = Main.class.getClassLoader().getResource("documents");
    static ObjectMapper jsonParser = new ObjectMapper()
            .findAndRegisterModules()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    public static CrptApi.DocumentRequest getDocumentById(int id) throws IOException {
        File file = loadFile(id);
        if (!file.exists()) {
            log.log(Level.WARNING, String.format("File not found: {%s}", file.getPath()));
            throw new IOException(String.format("File not found: %s", file.getPath()));
        }
        try {
            JsonNode jsonNode = jsonParser.readValue(file, JsonNode.class);
            return jsonParser.convertValue(jsonNode.get("document_request"), CrptApi.DocumentRequest.class);
        } catch (IOException e) {
            log.log(Level.WARNING, String.format("Error reading or parsing file: {%s}", file.getPath()), e);
            throw new IOException(String.format("File not found: %s", file.getPath()));
        }
    }

    private static File loadFile(int id) throws IOException {
        if (DOCUMENTS_FOLDER_URL == null) {
            log.log(Level.WARNING, "Documents folder URL is null");
            throw new IOException("Documents folder URL is null");
        }

        String filePath = String.format(DOCUMENTS_FOLDER_PATH_TEMPLATE, id);
        return new File(DOCUMENTS_FOLDER_URL.getPath(), filePath);
    }
}