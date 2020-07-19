package com.codeperfector.examples.kafkaconsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaConsumerApplication implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerApplication.class);

	@Autowired
	private ConsumerConfig consumerConfig;

	public static void main(String[] args) {
		SpringApplication.run(KafkaConsumerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		LOGGER.info("properties: " + consumerConfig.getConsumer());
	}
}
