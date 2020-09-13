package com.codeperfector.examples.kafkaconsumer;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "kafka")
public class AppConfig {

    public List<String> topics;
    public Map<String, String> consumer = new HashMap<>();
    public Map<String, String> producer = new HashMap<>();
}
