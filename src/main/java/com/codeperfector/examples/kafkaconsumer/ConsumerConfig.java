package com.codeperfector.examples.kafkaconsumer;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "kafka")
public class ConsumerConfig {

    public Map<String, String> consumer = new HashMap<>();
}
