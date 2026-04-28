package com.faisal.dev.atsanalyzer.dto;

import java.time.LocalDateTime;

public record ResumeStatusResponse(
        Long resumeId,
        String status,
        LocalDateTime uploadedAt,
        LocalDateTime processedAt,
        String failureReason
) {
}
