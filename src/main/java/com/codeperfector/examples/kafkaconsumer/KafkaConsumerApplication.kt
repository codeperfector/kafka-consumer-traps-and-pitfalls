package com.codeperfector.examples.kafkaconsumer

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.util.ArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootApplication
open class KafkaConsumerApplication(private val appConfig: AppConfig) : CommandLineRunner {
    @Override
    @Throws(Exception::class)
    override fun run(vararg args: String?) {
        LOGGER.info("topics: " + appConfig.topics)
        LOGGER.info("consumer properties: " + appConfig.consumer)
        LOGGER.info("producer properties: " + appConfig.producer)
        kickOffTest()
    }

    private fun kickOffTest() {
        val numConsumers = 10
        val executor: ExecutorService = Executors.newFixedThreadPool(numConsumers)
        val producer = TestProducer(appConfig.topics!!, appConfig.producer as MutableMap<String?, String?>)
        producer.produceMessages(10000)
        producer.close()
        val consumers: MutableList<TestConsumer> = ArrayList()
        for (i in 0 until numConsumers) {
            var failureProbability = 0.0
            var delayMillis = 0L
            if (i == 0) {
                failureProbability = 1.0
                delayMillis = 100000
            }
            val throwExceptions = false
            submitTestConsumer(executor, consumers, i, failureProbability, delayMillis, throwExceptions)
        }
        Runtime.getRuntime().addShutdownHook(Thread {
            for (consumer in consumers) {
                consumer.shutdown()
            }
            executor.shutdown()
        })
        try {
            executor.awaitTermination(5000, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun submitTestConsumer(
        executor: ExecutorService,
        consumers: MutableList<TestConsumer>,
        i: Int,
        failureProbability: Double,
        delayMillis: Long,
        throwExceptions: Boolean
    ) {
        val consumer = TestConsumer(
            i, failureProbability, delayMillis, throwExceptions,
            appConfig.topics!!, appConfig.consumer as MutableMap<String?, String?>
        )
        consumers.add(consumer)
        executor.submit(consumer)
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(KafkaConsumerApplication::class.java)
        fun main(args: Array<String?>?) {
            SpringApplication.run(KafkaConsumerApplication::class.java as Class<CommandLineRunner>, *(args as Array<String>))
        }
    }
}