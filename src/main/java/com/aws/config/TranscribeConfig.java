package com.aws.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.transcribe.TranscribeClient;

@Configuration
public class TranscribeConfig extends AwsClientConfig {

    @Bean
    public TranscribeClient transcribeClient() {
        return TranscribeClient.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
    }
}

