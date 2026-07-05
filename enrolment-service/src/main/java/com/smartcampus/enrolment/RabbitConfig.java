package com.smartcampus.enrolment;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public TopicExchange campusExchange() {
        return new TopicExchange("exchange.smartcampus", true, false);
    }
}