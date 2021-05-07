package com.codeperfector.examples.kafkaconsumer

import lombok.extern.slf4j.Slf4j
import mu.KotlinLogging.logger
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.HashMap
import java.util.stream.IntStream

/**
 * Produce numMessages messages to numTopics topics
 */
@Slf4j
class TestProducer(private val topics: List<String>, producerProps: MutableMap<String?, String?>) {
    private val producer: Producer<String, String>
    val log = logger{}
    fun produceMessages(numMessages: Int) {
        IntStream.range(1, numMessages + 1)
            .mapToObj { i -> "message$i" }
            .forEach { msg ->
                for (topic in topics) {
                    log.info("Producing message {} to topic {}", msg, topic)
                    producer.send(ProducerRecord(topic, msg, msg))
                }
            }
        producer.flush()
    }

    fun close() {
        producer.close()
    }

    init {
        val props: MutableMap<String, Object> = mutableMapOf()
        props.putAll(producerProps as Map<out String, Object>)
        props.put("key.serializer", StringSerializer::class.java.getName() as Object)
        props.put("value.serializer", StringSerializer::class.java.getName() as Object)
        producer = KafkaProducer<String, String>(props as Map<String, Any>?)
    }
}