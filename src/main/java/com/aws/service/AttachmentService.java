package com.aws.service;

import com.aws.entity.Attachment;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AttachmentService {
    Attachment uploadFile(MultipartFile file) throws IOException;
    String generateDownloadUrl(String fileId);
}
