package com.aws.controller;

import com.aws.entity.Attachment;
import com.aws.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileOrganizationController {

    private final AttachmentService attachmentService;

    @PostMapping("/upload")
    public ResponseEntity<Attachment> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(attachmentService.uploadFile(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<String> getUrl(@PathVariable("fileId") String fileId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attachmentService.generateDownloadUrl(fileId));
    }
}
