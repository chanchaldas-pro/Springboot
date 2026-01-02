package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${razorpay.key.id}")
    public String razorpayKeyId;
    @Value("${razorpay.key.secret}")
    public String razorpayKeySecret;

    @Bean
    public WebClient razorpayWebClient(WebClient.Builder builder) {
        System.out.println(razorpayKeyId+razorpayKeySecret);
        return builder
                .baseUrl("https://api.razorpay.com/v1")
                .defaultHeaders(headers -> {
                    headers.setBasicAuth(razorpayKeyId, razorpayKeySecret);
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofSeconds(5)) // gateway slow handling
                ))
                .build();
    }

}

