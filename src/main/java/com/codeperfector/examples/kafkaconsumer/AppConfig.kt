package com.codeperfector.examples.kafkaconsumer

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component


@Component
@ConfigurationProperties(prefix = "kafka")
class AppConfig {
    var topics: MutableList<String>? = null
    var consumer: MutableMap<String?, String?> = mutableMapOf()
    var producer: MutableMap<String?, String?> = mutableMapOf()
}