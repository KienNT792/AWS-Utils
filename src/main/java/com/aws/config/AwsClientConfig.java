package com.aws.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

@Configuration
public abstract class AwsClientConfig {
    @Value("${aws.access.key.id}")
    protected String accessKey;

    @Value("${aws.secret.access.key}")
    protected String secretKey;

    @Value("${aws.region}")
    protected String region;

    @Getter
    @Value("${aws.s3.bucket.name}")
    protected String bucketName;

    protected AwsCredentialsProvider getCredentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );
    }
    protected Region getRegion() {
        return Region.of(region);
    }
}
