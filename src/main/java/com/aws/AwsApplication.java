package com.aws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class AwsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AwsApplication.class, args);
    }

}
