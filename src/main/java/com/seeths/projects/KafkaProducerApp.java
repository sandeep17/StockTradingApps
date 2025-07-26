package com.seeths.projects;

/**
 * Producer code to send trading events
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Instant;
import java.util.Properties;
import java.util.Random;

public class KafkaProducerApp {
    private static final String TOPIC = "trading-events";
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private static final String[] SYMBOLS = {"AAPL", "GOOG", "MSFT", "TSLA", "AMZN"};

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try (Producer<String, String> producer = new KafkaProducer<>(props)) {
            Random random = new Random();
            for (int i = 0; i < 10; i++) {
                TradingEvent event = new TradingEvent(
                        SYMBOLS[random.nextInt(SYMBOLS.length)],
                        100 + random.nextDouble() * 1000,
                        1 + random.nextInt(100),
                        random.nextBoolean() ? "BUY" : "SELL",
                        Instant.now()
                );
                String eventJson = MAPPER.writeValueAsString(event);
                ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, event.getSymbol(), eventJson);
                producer.send(record, (metadata, exception) -> {
                    if (exception == null) {
                        System.out.printf("Sent record to topic %s partition %d offset %d%n",
                                metadata.topic(), metadata.partition(), metadata.offset());
                    } else {
                        exception.printStackTrace();
                    }
                });
                Thread.sleep(500);
            }
        }
    }
}