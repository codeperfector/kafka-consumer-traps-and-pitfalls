package com.codeperfector.examples.kafkaconsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class KafkaConsumerApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerApplication.class);

    private final AppConfig appConfig;

	public KafkaConsumerApplication(AppConfig appConfig) {
		this.appConfig = appConfig;
	}

	public static void main(String[] args) {
        SpringApplication.run(KafkaConsumerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("topics: " + appConfig.getTopics());
        LOGGER.info("consumer properties: " + appConfig.getConsumer());
        LOGGER.info("producer properties: " + appConfig.getProducer());
        kickOffTest();
    }

    private void kickOffTest() {

        int numConsumers = 3;
        ExecutorService executor = Executors.newFixedThreadPool(numConsumers);

        TestProducer producer = new TestProducer(appConfig.getTopics(), appConfig.getProducer());
        producer.produceMessages(10_000);
        producer.close();

        final List<TestConsumer> consumers = new ArrayList<>();

        for (int i = 0; i < numConsumers; i++) {
            double failureProbability = 0.0;
            long delayMillis = 0L;
            if (i == 0) {
                failureProbability = 1.0;
                delayMillis = 100000;
            }
            boolean throwExceptions = false;
            submitTestConsumer(executor, consumers, i, failureProbability, delayMillis, throwExceptions);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (TestConsumer consumer : consumers) {
                consumer.shutdown();
            }
            executor.shutdown();
        }));

        try {
            executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void submitTestConsumer(ExecutorService executor, List<TestConsumer> consumers, int i, double failureProbability, long delayMillis, boolean throwExceptions) {
        TestConsumer consumer = new TestConsumer(i, failureProbability, delayMillis, throwExceptions,
                appConfig.getTopics(), appConfig.getConsumer());
        consumers.add(consumer);
        executor.submit(consumer);
    }
}
