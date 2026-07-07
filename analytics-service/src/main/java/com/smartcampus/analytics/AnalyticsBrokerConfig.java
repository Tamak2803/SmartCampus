package com.smartcampus.analytics;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnalyticsBrokerConfig {

    public static final String EXCHANGE_NAME = "exchange.smartcampus";
    public static final String QUEUE_NAME = "queue.analytics";
    public static final String ROUTING_KEY = "student.enrolled";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue analyticsQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding binding(Queue analyticsQueue, TopicExchange exchange) {
        return BindingBuilder.bind(analyticsQueue).to(exchange).with(ROUTING_KEY);
    }
}