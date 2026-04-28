package com.faisal.dev.atsanalyzer.service.impl;

import com.faisal.dev.atsanalyzer.dto.ResumeAnalysisResponse;
import com.faisal.dev.atsanalyzer.dto.ResumeStatusResponse;
import com.faisal.dev.atsanalyzer.dto.ResumeUploadResponse;
import com.faisal.dev.atsanalyzer.entity.ProcessingStatus;
import com.faisal.dev.atsanalyzer.entity.Resume;
import com.faisal.dev.atsanalyzer.exception.ResourceNotFoundException;
import com.faisal.dev.atsanalyzer.mapper.ResumeAnalysisDashboardMapper;
import com.faisal.dev.atsanalyzer.repository.ResumeAnalysisRepository;
import com.faisal.dev.atsanalyzer.repository.ResumeRepository;
import com.faisal.dev.atsanalyzer.repository.SuggestionRepository;
import com.faisal.dev.atsanalyzer.scoring.engine.AtsScoringEngine;
import com.faisal.dev.atsanalyzer.scoring.engine.ScoringContext;
import com.faisal.dev.atsanalyzer.scoring.result.DetailedScoreResult;
import com.faisal.dev.atsanalyzer.scoring.suggestion.ResumeSuggestionGenerator;
import com.faisal.dev.atsanalyzer.service.ResumeProcessingService;
import com.faisal.dev.atsanalyzer.service.ResumeService;
import com.faisal.dev.atsanalyzer.service.StorageService;
import com.faisal.dev.atsanalyzer.service.StoredFileReference;
import com.faisal.dev.atsanalyzer.validation.ResumeFileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeServiceImpl implements ResumeService {

    private final ResumeFileValidator resumeFileValidator;

    private final StorageService storageService;

    private final ResumeRepository resumeRepository;

    private final ResumeAnalysisRepository
            resumeAnalysisRepository;

    private final SuggestionRepository suggestionRepository;

    private final ResumeProcessingService
            resumeProcessingService;

    private final AtsScoringEngine atsScoringEngine;

    private final ResumeAnalysisDashboardMapper
            dashboardMapper;

    private final ResumeSuggestionGenerator
            resumeSuggestionGenerator;

    @Override
    public ResumeUploadResponse uploadResume(MultipartFile file) {

        log.info(
                "Resume upload started. originalFileName={}",
                file.getOriginalFilename()
        );

        resumeFileValidator.validate(file);

        StoredFileReference storedFile =
                Objects.requireNonNull(
                        storageService.storeFile(file),
                        "Storage service returned null stored file reference"
                );

        Resume resume = Resume.builder()
                .originalFileName(file.getOriginalFilename())
                .storedFileName(storedFile.storedFileName())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .storagePath(storedFile.storagePath())
                .processingStatus(ProcessingStatus.UPLOADED.name())
                .uploadedAt(LocalDateTime.now())
                .build();

        Resume savedResume = resumeRepository.save(resume);

        log.info(
                "Resume uploaded successfully. resumeId={}",
                savedResume.getId()
        );

        resumeProcessingService.processResume(
                savedResume.getId()
        );

        return new ResumeUploadResponse(
                savedResume.getId(),
                savedResume.getProcessingStatus()
        );
    }

    @Override
    public ResumeStatusResponse getResumeStatus(
            Long resumeId
    ) {

        Resume resume = getResumeOrThrow(resumeId);

        return new ResumeStatusResponse(
                resume.getId(),
                resume.getProcessingStatus(),
                resume.getUploadedAt(),
                resume.getProcessedAt(),
                resume.getFailureReason()
        );
    }

    @Override
    public ResumeAnalysisResponse getResumeAnalysis(
            Long resumeId
    ) {

        Resume resume = getResumeOrThrow(resumeId);

        var analysisOptional = resumeAnalysisRepository
                .findByResumeId(resumeId);

        if (analysisOptional.isPresent()) {

            var analysis = analysisOptional.get();

            DetailedScoreResult detailedScoreResult =
                    atsScoringEngine.calculateScores(
                            ScoringContext.of(
                                    analysis.getExtractedText(),
                                    analysis.getCleanedText(),
                                    analysis.getTotalWords(),
                                    analysis.getTotalCharacters()
                            )
                    );

            return dashboardMapper.toResponse(
                    resume,
                    analysis,
                    detailedScoreResult,
                    resumeSuggestionGenerator.generate(
                            resume,
                            detailedScoreResult
                    )
            );
        }

        return dashboardMapper.toUnavailableResponse(
                resume,
                suggestionRepository
                        .findByResumeIdOrderByCreatedAtAsc(
                                resumeId
                        )
        );
    }

    private Resume getResumeOrThrow(Long resumeId) {

        return resumeRepository.findById(resumeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resume not found"
                        )
                );
    }
}
