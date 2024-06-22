package com.kstrinadka;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * CrptApi is a Java library for interacting with the CRPT monitoring system API.
 * It supports rate limiting and automatic error handling.
 */
public class CrptApi {

    private static final Logger log = Logger.getLogger(CrptApi.class.getName());

    private final static String BASE_URL = "https://ismp.crpt.ru/api/v3/lk/documents";
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;
    private static final int DEFAULT_REQUEST_LIMIT = 50;
    private static final int MAX_REQUESTS_TO_WAIT = 1000;   // максимальное количество ожидающих исполнения запросов к API
    private static final int HTTP_OK_STATUS = 200;


    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private final int requestLimit;
    private final AtomicInteger requestCount;
    private final ReentrantLock lock;
    private final Condition condition;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public CrptApi() {
        this(DEFAULT_TIME_UNIT, DEFAULT_REQUEST_LIMIT);
    }

    /**
     * Constructs a CrptApi instance with specified time unit and request limit.
     *
     * @param timeUnit     the time unit for rate limiting
     * @param requestLimit the maximum number of requests per time unit
     */
    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        client = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
        this.requestLimit = requestLimit;
        this.requestCount = new AtomicInteger(0);
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();

        initRateLimitingScheduler(timeUnit);
    }

    private void initRateLimitingScheduler(TimeUnit timeUnit) {
        scheduler.scheduleAtFixedRate(() -> {
            lock.lock();
            try {
                requestCount.set(0);
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }, 0, 1, timeUnit);
    }

    /**
     * Checks and waits if the rate limit is reached.
     *
     * @throws InterruptedException       if interrupted while waiting
     * @throws TooManyRequestsException if the maximum number of waiting requests is exceeded
     */
    private void rateLimitingCondition() throws InterruptedException, TooManyRequestsException {
        if (requestCount.get() >= MAX_REQUESTS_TO_WAIT) {
            throw new TooManyRequestsException("Maximum number of requests to wait for has been exceeded.");
        }

        lock.lock();
        try {
            while (requestCount.get() >= requestLimit) {
                condition.await();
            }
            requestCount.incrementAndGet();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sends a request to create a document.
     *
     * @param documentRequest the document request to be sent
     * @return the HTTP response from the API
     * @throws InterruptedException       if interrupted while waiting
     * @throws IOException                if an I/O error occurs
     * @throws TooManyRequestsException if the maximum number of waiting requests is exceeded
     */
    public HttpResponse<String> createDocument(DocumentRequest documentRequest)
            throws InterruptedException, IOException, TooManyRequestsException {
        rateLimitingCondition();

        HttpResponse<String> response = client.send(
                createRequestFromDocument(documentRequest),
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != HTTP_OK_STATUS) {
            ErrorResponse errorResponse = objectMapper.readValue(
                    response.body(), ErrorResponse.class
            );
            log.log(Level.WARNING, "Status code: " + errorResponse.code());
            log.log(Level.WARNING, "Error: " + errorResponse.error());
            log.log(Level.WARNING, "Message: " + errorResponse.message());
        }
        return response;
    }

    /**
     * Creates an HTTP request from the document request.
     *
     * @param documentRequest the document request
     * @return the HTTP request
     * @throws JsonProcessingException if an error occurs while processing JSON
     */
    private HttpRequest createRequestFromDocument(DocumentRequest documentRequest) throws JsonProcessingException {
        String uriWithSignature = String.format(
                "%s?signature=%s",
                BASE_URL + "/create",
                documentRequest.signature()
        );
        return HttpRequest.newBuilder()
                .uri(URI.create(uriWithSignature))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(documentRequest.document())))
                .build();
    }


    public record DocumentRequest(String signature, Document document) {}

    public record Document(
            Description description,
            String docId,
            String docStatus,
            String docType,
            @JsonProperty("importRequest") boolean importRequest,
            String ownerInn,
            String participantInn,
            String producerInn,
            String productionDate,
            String productionType,
            Product[] products,
            String regDate,
            String regNumber
    ) {}


    public record Description(@JsonProperty("participantInn") String participantInn) {}

    public record Product(
            String certificateDocument,
            String certificateDocumentDate,
            String certificateDocumentNumber,
            String ownerInn,
            String producerInn,
            String productionDate,
            String tnvedCode,
            String uitCode,
            String uituCode
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ErrorResponse(String code, String error, String message) {}

    static class TooManyRequestsException extends Exception {
        public TooManyRequestsException(String message) {
            super(message);
        }
    }

}
