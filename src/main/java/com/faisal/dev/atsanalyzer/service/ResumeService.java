package com.faisal.dev.atsanalyzer.service;

import com.faisal.dev.atsanalyzer.dto.ResumeUploadResponse;
import com.faisal.dev.atsanalyzer.dto.ResumeAnalysisResponse;
import com.faisal.dev.atsanalyzer.dto.ResumeStatusResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ResumeService {

    ResumeUploadResponse uploadResume(MultipartFile file);

    ResumeStatusResponse getResumeStatus(Long resumeId);

    ResumeAnalysisResponse getResumeAnalysis(Long resumeId);
}
