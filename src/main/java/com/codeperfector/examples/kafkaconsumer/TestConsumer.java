package com.codeperfector.examples.kafkaconsumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TestConsumer implements Runnable {

    private final KafkaConsumer<String, String> consumer;
    private final List<String> topics;
    private int id;
    private long delayMillis = 0;
    private double failureProbability;
    private boolean throwExceptions = false;
    private volatile boolean keepRunning = true;

    public TestConsumer(int id, double failureProbability,
                        long delayMillis, boolean throwExceptions,
                        List<String> topics,
                        Map<String, String> consumerProps) {
        this.id = id;
        this.delayMillis = delayMillis;
        this.throwExceptions = throwExceptions;
        this.topics = topics;

        // KafkaConsumer constructor accepts Map<String, Object> so we have to do a type conversion here. Yay Java!! :(
        Map<String, Object> props = new HashMap<>();
        consumerProps.forEach(props::put);

        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        this.consumer = new KafkaConsumer<>(props);
        this.failureProbability = failureProbability;
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(topics);

            while (keepRunning) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10L));
                for (ConsumerRecord<String, String> record : records) {
                    if (Math.random() < failureProbability) {
                        if (throwExceptions) {
                            throw new RuntimeException("Consumer has failed");
                        }
                        if (delayMillis > 0) {
                            try {
                                Thread.sleep(delayMillis);
                            } catch (InterruptedException e) {
                                //ignore
                            }
                        }
                    }
                    log.info("Consumed data: consumer: {}, topic: {}, partition: {}, offset: {}, value: {}, delay: {}",
                            id, record.topic(), record.partition(), record.offset(), record.value(), delayMillis);
                }
                consumer.commitSync();
            }
        } catch (WakeupException e) {
            log.info("Shutting down consumer " + id);
        } catch (RuntimeException e) {
            // If this throws an exception, the consumer dies and leaves the consumer group
            // In that case the consumer must be restarted after some period of time.
            // It is possible to do this in a Kubernetes environment with appropriate policies,
            // but in our case we'll just restart the thread that uses this consumer.
            log.info("Consumer {} experienced an exception ", id, e);
        } finally {
            consumer.close();
        }
    }

    public void shutdown() {
        keepRunning = false;
        consumer.wakeup();
    }
}