package com.codeperfector.examples.kafkaconsumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Produce numMessages messages to numTopics topics
 */
@Slf4j
public class TestProducer {

    private List<String> topics;
    private Producer<String, String> producer;


    public TestProducer(List<String> topics, Map<String, String> producerProps) {
        this.topics = topics;
        Map<String, Object> props = new HashMap<>();
        producerProps.forEach(props::put);

        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        this.producer = new KafkaProducer<>(props);
    }

    public void produceMessages(int numMessages) {
        IntStream.range(1, numMessages+1)
        .mapToObj(i -> "message" + i)
        .forEach(msg -> {
            for (String topic : topics) {
                log.info("Producing message {} to topic {}", msg, topic);
                producer.send(new ProducerRecord<>(topic, msg, msg));
            }
        });
        producer.flush();
    }

    public void close() {
        producer.close();
    }
}
