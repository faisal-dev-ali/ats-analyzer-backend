package com.faisal.dev.atsanalyzer.service.impl;

import com.faisal.dev.atsanalyzer.entity.AtsScore;
import com.faisal.dev.atsanalyzer.entity.ProcessingStatus;
import com.faisal.dev.atsanalyzer.entity.Resume;
import com.faisal.dev.atsanalyzer.entity.ResumeAnalysis;
import com.faisal.dev.atsanalyzer.exception.ResourceNotFoundException;
import com.faisal.dev.atsanalyzer.parser.ParsedResumeContent;
import com.faisal.dev.atsanalyzer.parser.ResumeParsingService;
import com.faisal.dev.atsanalyzer.repository.AtsScoreRepository;
import com.faisal.dev.atsanalyzer.repository.ResumeAnalysisRepository;
import com.faisal.dev.atsanalyzer.repository.ResumeRepository;
import com.faisal.dev.atsanalyzer.repository.SuggestionRepository;
import com.faisal.dev.atsanalyzer.scoring.engine.AtsScoringEngine;
import com.faisal.dev.atsanalyzer.scoring.engine.ScoringContext;
import com.faisal.dev.atsanalyzer.scoring.constants.ScoreCategory;
import com.faisal.dev.atsanalyzer.scoring.result.DetailedScoreResult;
import com.faisal.dev.atsanalyzer.scoring.result.ScoreResult;
import com.faisal.dev.atsanalyzer.scoring.suggestion.ResumeSuggestionGenerator;
import com.faisal.dev.atsanalyzer.service.ResumeProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeProcessingServiceImpl
        implements ResumeProcessingService {

    private final ResumeRepository resumeRepository;

    private final ResumeAnalysisRepository
            resumeAnalysisRepository;

    private final AtsScoreRepository atsScoreRepository;

    private final SuggestionRepository suggestionRepository;

    private final ResumeParsingService
            resumeParsingService;

    private final AtsScoringEngine atsScoringEngine;

    private final ResumeSuggestionGenerator
            resumeSuggestionGenerator;

    @Override
    @Async("resumeTaskExecutor")
    public void processResume(Long resumeId) {

        log.info(
                "Resume processing started. resumeId={}",
                resumeId
        );

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resume not found"
                        )
                );

        try {

            markProcessing(resume);

            ParsedResumeContent parsedContent =
                    resumeParsingService.parse(resume);

            saveAnalysis(resume, parsedContent);

            DetailedScoreResult scoreResult =
                    atsScoringEngine.calculateScores(
                            ScoringContext.of(
                                    parsedContent.extractedText(),
                                    parsedContent.cleanedText(),
                                    parsedContent.totalWords(),
                                    parsedContent.totalCharacters()
                            )
                    );

            saveScore(resume, scoreResult);
            saveSuggestions(resume, scoreResult);
            markCompleted(resume);

            log.info(
                    "Resume processing completed successfully. resumeId={}",
                    resumeId
            );

        } catch (Exception ex) {

            log.error(
                    "Resume processing failed. resumeId={}",
                    resumeId,
                    ex
            );

            markFailed(resume, ex.getMessage());
        }
    }

    private void markProcessing(Resume resume) {

        resume.setProcessingStatus(
                ProcessingStatus.PROCESSING.name()
        );
        resume.setFailureReason(null);
        resume.setProcessedAt(null);

        resumeRepository.save(resume);
    }

    private void markCompleted(Resume resume) {

        resume.setProcessingStatus(
                ProcessingStatus.COMPLETED.name()
        );
        resume.setFailureReason(null);
        resume.setProcessedAt(LocalDateTime.now());

        resumeRepository.save(resume);
    }

    private void markFailed(
            Resume resume,
            String failureReason
    ) {

        resume.setProcessingStatus(
                ProcessingStatus.FAILED.name()
        );
        resume.setFailureReason(failureReason);
        resume.setProcessedAt(LocalDateTime.now());

        resumeRepository.save(resume);
    }

    private void saveAnalysis(
            Resume resume,
            ParsedResumeContent parsedContent
    ) {

        ResumeAnalysis analysis =
                resumeAnalysisRepository.findByResumeId(
                                resume.getId()
                        )
                        .orElseGet(() ->
                                ResumeAnalysis.builder()
                                        .resume(resume)
                                        .build()
                        );

        analysis.setExtractedText(
                parsedContent.extractedText()
        );
        analysis.setCleanedText(
                parsedContent.cleanedText()
        );
        analysis.setTotalPages(
                parsedContent.totalPages()
        );
        analysis.setTotalWords(
                parsedContent.totalWords()
        );
        analysis.setTotalCharacters(
                parsedContent.totalCharacters()
        );
        analysis.setCreatedAt(LocalDateTime.now());

        resumeAnalysisRepository.save(analysis);
    }

    private void saveScore(
            Resume resume,
            DetailedScoreResult scoreResult
    ) {

        AtsScore atsScore = atsScoreRepository
                .findByResumeId(resume.getId())
                .orElseGet(() ->
                        AtsScore.builder()
                                .resume(resume)
                                .build()
                );

        atsScore.setOverallScore(
                scoreResult.getOverallScore()
        );
        atsScore.setUniversalScore(
                scoreResult.getUniversalScore()
        );
        atsScore.setDomainRelevanceScore(
                scoreResult.getDomainRelevanceScore()
        );
        atsScore.setAtsCompatibilityScore(
                scoreFor(
                        scoreResult,
                        ScoreCategory.ATS_COMPATIBILITY
                )
        );
        atsScore.setImpactMetricsScore(
                scoreFor(
                        scoreResult,
                        ScoreCategory.IMPACT_METRICS
                )
        );
        atsScore.setReadabilityScore(
                scoreFor(
                        scoreResult,
                        ScoreCategory.READABILITY
                )
        );
        atsScore.setActionVerbScore(
                scoreFor(
                        scoreResult,
                        ScoreCategory.ACTION_VERBS
                )
        );
        atsScore.setContentClarityScore(
                scoreFor(
                        scoreResult,
                        ScoreCategory.CONTENT_CLARITY
                )
        );
        atsScore.setResumeLengthScore(
                scoreFor(
                        scoreResult,
                        ScoreCategory.RESUME_LENGTH
                )
        );
        atsScore.setStructureQualityScore(
                scoreFor(
                        scoreResult,
                        ScoreCategory.STRUCTURE_QUALITY
                )
        );
        atsScore.setTechnicalSkillsScore(null);
        atsScore.setKeywordDensityScore(null);
        atsScore.setDetectedDomain(
                scoreResult.getResumeInsights()
                        .detectedDomainLabel()
        );
        atsScore.setInferredSeniority(
                scoreResult.getResumeInsights()
                        .seniorityLevel()
                        .getLabel()
        );
        atsScore.setInferredExperienceYears(
                scoreResult.getResumeInsights()
                        .inferredExperienceYears()
        );
        atsScore.setCreatedAt(LocalDateTime.now());

        atsScoreRepository.save(atsScore);
    }

    private void saveSuggestions(
            Resume resume,
            DetailedScoreResult scoreResult
    ) {

        suggestionRepository.deleteByResumeId(
                resume.getId()
        );

        suggestionRepository.saveAll(
                resumeSuggestionGenerator.generate(
                        resume,
                        scoreResult
                )
        );
    }

    private Double scoreFor(
            DetailedScoreResult scoreResult,
            ScoreCategory scoreCategory
    ) {

        return scoreResult.getCategoryScores()
                .stream()
                .filter(categoryScore ->
                        categoryScore.getCategory() ==
                                scoreCategory
                )
                .map(ScoreResult::getScore
                )
                .findFirst()
                .orElse(null);
    }
}
