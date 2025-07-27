package com.seeths.projects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.producer.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.time.Instant;
import java.util.Properties;
import java.util.Random;

public class KafkaProducerApp {
    private static final String TOPIC = "trading-events";
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private static final String[] SYMBOLS = {"AAPL", "GOOG", "MSFT", "TSLA", "AMZN"};
    private static final String ALPHA_VANTAGE_API_KEY = "88FSHYRH4SA95QO7"; // Replace with your API key
    private static final String ALPHA_VANTAGE_URL = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s";

    private static double getRealtimePrice(String symbol) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = String.format(ALPHA_VANTAGE_URL, symbol, ALPHA_VANTAGE_API_KEY);
            HttpGet request = new HttpGet(url);
            String response = client.execute(request, httpResponse ->
                    EntityUtils.toString(httpResponse.getEntity()));

            JsonNode root = MAPPER.readTree(response);
            JsonNode globalQuote = root.get("Global Quote");
            if (globalQuote != null) {
                String price = globalQuote.get("05. price").asText();
                return Double.parseDouble(price);
            }
            throw new RuntimeException("Unable to fetch price for " + symbol);
        }
    }

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try (Producer<String, String> producer = new KafkaProducer<>(props)) {
            Random random = new Random();
            for (int i = 0; i < 10; i++) {
                String symbol = SYMBOLS[random.nextInt(SYMBOLS.length)];
                double price = getRealtimePrice(symbol);

                TradingEvent event = new TradingEvent(
                        symbol,
                        price,
                        1 + random.nextInt(100),
                        random.nextBoolean() ? "BUY" : "SELL",
                        Instant.now()
                );

                String eventJson = MAPPER.writeValueAsString(event);
                ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, event.getSymbol(), eventJson);
                producer.send(record, (metadata, exception) -> {
                    if (exception == null) {
                        System.out.printf("Sent record to topic %s partition %d offset %d with the following key %s %n",
                                metadata.topic(), metadata.partition(), metadata.offset(), event.getSymbol());
                    } else {
                        exception.printStackTrace();
                    }
                });
                // Sleep for 12 seconds to respect Alpha Vantage's rate limit of 5 calls per minute on free tier
                Thread.sleep(12000);
            }
        }
    }
}