package com.aws.controller;

import com.aws.service.TranscribeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transcribe")
@RequiredArgsConstructor
public class TranscribeController {
    private final TranscribeService transcribeService;

    @PostMapping("/transcribe/{fileId}")
    public ResponseEntity<?> startTranscribe(@PathVariable String fileId) {
        String jobName = "job-" + fileId;
        transcribeService.startTranscription(jobName, fileId, "en-US");
        return ResponseEntity.ok("Transcribe job started: " + jobName);
    }

    @GetMapping("/transcribe/{jobName}/status")
    public ResponseEntity<?> getStatus(@PathVariable String jobName) {
        var job = transcribeService.getTranscriptionJob(jobName);
        return ResponseEntity.ok(job.transcriptionJobStatusAsString());
    }

    @GetMapping("/transcribe/{jobName}")
    public ResponseEntity<String> getTranscription(@PathVariable String jobName) {
        try {
            String transcriptJson = transcribeService.getTranscribe(jobName);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(transcriptJson);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
