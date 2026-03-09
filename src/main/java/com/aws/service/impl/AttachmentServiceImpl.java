package com.aws.service.impl;

import com.aws.config.S3Config;
import com.aws.entity.Attachment;
import com.aws.repository.AttachmentRepository;
import com.aws.service.AttachmentService;
import com.aws.service.TranscribeService;
import com.aws.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository repository;
    private final S3Config s3Config;
    private final TranscribeService transcribeService;

    @Override
    public Attachment uploadFile(MultipartFile file) throws IOException {
        String mimeType = file.getContentType();
        String fileTypeFolder = Constant.MimeTypeEnum.classify(mimeType).name().toLowerCase();

        String originalFilename = file.getOriginalFilename();
        String fileUUID = UUID.randomUUID().toString();
        String fileExtension = getFileExtension(originalFilename);

        String s3Key = fileTypeFolder + "/" + fileUUID + fileExtension;

        // Upload file lên S3
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(s3Config.getBucketName())
                .key(s3Key)
                .contentType(mimeType)
                .build();

        PutObjectResponse putObjectResponse = s3Config.s3Client().putObject(putRequest, RequestBody.fromBytes(file.getBytes()));

        Attachment attachment = new Attachment();
        attachment.setFileId(fileUUID);
        attachment.setFileName(originalFilename);
        attachment.setFileType(mimeType);

        Attachment savedAttachment = repository.save(attachment);

        // Gọi Transcribe sau khi upload thành công (nếu file là video)
        if (Constant.MimeTypeEnum.classify(mimeType) == Constant.FileCategory.VIDEOS) {
            String jobName = "transcribe-job-" + fileUUID;
            transcribeService.startTranscriptionAsync(jobName, savedAttachment.getFileId(), "ko-KR")
                    .thenAccept(transcriptionId -> {
                        try {
                            Attachment attachmentToUpdate = repository.findById(savedAttachment.getId())
                                    .orElseThrow(() -> new RuntimeException("Attachment not found when updating transcriptionId"));
                            attachmentToUpdate.setTranscriptionId(transcriptionId);
                            repository.save(attachmentToUpdate);
                        } catch (Exception ex) {
                            System.err.println("Failed to update transcriptionId: " + ex.getMessage());
                        }
                    });
        }
        return savedAttachment;
    }

    @Override
    public String generateDownloadUrl(String fileId) {
        Attachment attachment = repository.findByFileId(fileId);
        if (attachment == null) {
            throw new RuntimeException("Attachment not found");
        }

        String fileTypeFolder = Constant.MimeTypeEnum.classify(attachment.getFileType()).name().toLowerCase();
        String s3Key = fileTypeFolder + "/" + attachment.getFileId() + getFileExtension(attachment.getFileName());

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Config.getBucketName())
                .key(s3Key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60))
                .getObjectRequest(getObjectRequest)
                .build();

        URL presignedUrl = s3Config.s3Presigner().presignGetObject(presignRequest).url();
        return presignedUrl.toString();
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex >= 0) ? fileName.substring(dotIndex) : "";
    }
}
