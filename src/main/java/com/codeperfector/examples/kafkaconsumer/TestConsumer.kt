package com.codeperfector.examples.kafkaconsumer

import mu.KLogger
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import mu.KotlinLogging.logger

class TestConsumer(
    val id: Int, failureProbability: Double,
    delayMillis: Long, throwExceptions: Boolean,
    topics: List<String>,
    consumerProps: MutableMap<String?, String?>
) : Runnable {
    private val consumer: KafkaConsumer<String, String>
    private val topics: List<String>
    private var delayMillis: Long = 0
    private val failureProbability: Double
    private var throwExceptions = false

    val log = logger{}

    @Volatile
    private var keepRunning = true
    @Override
    override fun run() {
        try {
            consumer.subscribe(topics)
            while (keepRunning) {
                val records: ConsumerRecords<String, String> = consumer.poll(Duration.ofSeconds(10L))
                for (record in records) {
                    if (Math.random() < failureProbability) {
                        if (throwExceptions) {
                            throw RuntimeException("Consumer has failed")
                        }
                        if (delayMillis > 0) {
                            try {
                                Thread.sleep(delayMillis)
                            } catch (e: InterruptedException) {
                                //ignore
                            }
                        }
                    }
                    if (delayMillis > 0) {
                        log.info(
                            "Consumed data: consumer: {}, topic: {}, partition: {}, offset: {}, value: {}, delay: {}",
                            id, record.topic(), record.partition(), record.offset(), record.value(), delayMillis
                        )
                    }
                }
                consumer.commitSync()
            }
        } catch (e: WakeupException) {
            log.info("Shutting down consumer $id")
        } catch (e: RuntimeException) {
            // If this throws an exception, the consumer dies and leaves the consumer group
            // In that case the consumer must be restarted after some period of time.
            // It is possible to do this in a Kubernetes environment with appropriate policies,
            // but in our case we'll just restart the thread that uses this consumer.
            log.info("Consumer {} experienced an exception ", id, e)
        } finally {
            consumer.close()
        }
    }

    fun shutdown() {
        consumer.metrics()
            .forEach { k, v -> log.info("consumer {}/{}: {}", id, k.name(), v.metricValue()) }
        keepRunning = false
        consumer.wakeup()
    }

    init {
        this.delayMillis = delayMillis
        this.throwExceptions = throwExceptions
        this.topics = topics

        // KafkaConsumer constructor accepts Map<String, Object> so we have to do a type conversion here. Yay Java!! :(
        val props: MutableMap<String, Object> = mutableMapOf()
        props.putAll(consumerProps as Map<out String, Object>)
        props.put("key.deserializer", StringDeserializer::class.java.getName() as Object)
        props.put("value.deserializer", StringDeserializer::class.java.getName() as Object)
        consumer = KafkaConsumer<String, String>(props as Map<String, Any>?)
        this.failureProbability = failureProbability
    }
}