package com.aws.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.transcribe.model.TranscriptionJob;

import java.util.concurrent.CompletableFuture;

@Service
public interface TranscribeService {
    String startTranscription(String jobName, String s3Uri, String languageCode);
    TranscriptionJob getTranscriptionJob(String jobName);
    String getTranscribe(String jobName);
    CompletableFuture<String> startTranscriptionAsync(String jobName, String fileId, String s);
}
