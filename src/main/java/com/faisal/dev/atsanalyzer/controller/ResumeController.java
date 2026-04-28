package com.faisal.dev.atsanalyzer.controller;

import com.faisal.dev.atsanalyzer.dto.ApiResponse;
import com.faisal.dev.atsanalyzer.dto.ResumeAnalysisResponse;
import com.faisal.dev.atsanalyzer.dto.ResumeStatusResponse;
import com.faisal.dev.atsanalyzer.dto.ResumeUploadResponse;
import com.faisal.dev.atsanalyzer.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
@Slf4j
@Validated
@CrossOrigin(
        origins = "*",
        allowedHeaders = "*",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        }
)
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ResumeUploadResponse>> uploadResume(
            @RequestParam("file") MultipartFile file
    ) {

        log.info("Received resume upload request");

        ResumeUploadResponse response =
                resumeService.uploadResume(file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Resume uploaded successfully",
                        response
                ));
    }

    @GetMapping("/{resumeId}/status")
    public ResponseEntity<ApiResponse<ResumeStatusResponse>>
    getResumeStatus(
            @PathVariable @Positive(message = "resumeId must be positive")
            Long resumeId
    ) {

        ResumeStatusResponse response =
                resumeService.getResumeStatus(resumeId);

        return ResponseEntity.ok(ApiResponse.success(
                "Resume status fetched successfully",
                response
        ));
    }

    @GetMapping("/{resumeId}/analysis")
    public ResponseEntity<ApiResponse<ResumeAnalysisResponse>>
    getResumeAnalysis(
            @PathVariable @Positive(message = "resumeId must be positive")
            Long resumeId
    ) {

        ResumeAnalysisResponse response =
                resumeService.getResumeAnalysis(resumeId);

        return ResponseEntity.ok(ApiResponse.success(
                "Resume analysis fetched successfully",
                response
        ));
    }
}
