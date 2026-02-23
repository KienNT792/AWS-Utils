package com.aws.service.impl;

import com.aws.entity.Attachment;
import com.aws.repository.AttachmentRepository;
import com.aws.service.TranscribeService;
import com.aws.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TranscribeServiceImpl implements TranscribeService {

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    private final TranscribeClient transcribeClient;
    private final AttachmentRepository attachmentRepository;
    private final S3Client s3Client;
    @Override
    public String startTranscription(String jobName, String fileId, String languageCode) {
        Attachment attachment = attachmentRepository.findByFileId(fileId);
        if (attachment == null) {
            throw new RuntimeException("File not found: " + fileId);
        }

        Constant.FileCategory category = Constant.MimeTypeEnum.classify(attachment.getFileType());
        String s3Key = category.name().toLowerCase() + "/" + attachment.getFileId() + getFileExtension(attachment.getFileName());
        String s3Uri = "s3://" + bucketName + "/" + s3Key;

        StartTranscriptionJobRequest request = StartTranscriptionJobRequest.builder()
                .transcriptionJobName(jobName)
                .languageCode(languageCode)
                .mediaFormat(getMediaFormat(getFileExtension(attachment.getFileName())))
                .media(Media.builder().mediaFileUri(s3Uri).build())
                .outputBucketName(bucketName)
                .outputKey("transcripts/" + jobName + ".json")
                .build();

        transcribeClient.startTranscriptionJob(request);
        return jobName;
    }

    @Override
    public TranscriptionJob getTranscriptionJob(String jobName) {
        GetTranscriptionJobRequest request = GetTranscriptionJobRequest.builder()
                .transcriptionJobName(jobName)
                .build();
        return transcribeClient.getTranscriptionJob(request).transcriptionJob();
    }

    @Override
    public String getTranscribe(String jobName) {
        String s3Key = "transcripts/" + jobName + ".json";
        System.out.println(s3Key);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        try (InputStream is = s3Client.getObject(getObjectRequest)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read transcript from S3", e);
        }
    }

    private MediaFormat getMediaFormat(String fileExtension) {
        if (fileExtension == null || fileExtension.isEmpty()) {
            throw new IllegalArgumentException("File extension is required for media format detection.");
        }
        String ext = fileExtension.toLowerCase().replace(".", "");
        return switch (ext) {
            case "mp4" -> MediaFormat.MP4;
            case "wav" -> MediaFormat.WAV;
            case "flac" -> MediaFormat.FLAC;
            case "webm" -> MediaFormat.WEBM;
            default -> throw new IllegalArgumentException("Unsupported media format: " + fileExtension);
        };
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex >= 0) ? fileName.substring(dotIndex) : "";
    }

    @Async
    public CompletableFuture<String> startTranscriptionAsync(String jobName, String fileId, String languageCode) {
        try {
            String transcriptionId = startTranscription(jobName, fileId, languageCode);
            return CompletableFuture.completedFuture(transcriptionId);
        } catch (Exception e) {
            System.err.println("Error in async transcription start: " + e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
                                                                                           
}
