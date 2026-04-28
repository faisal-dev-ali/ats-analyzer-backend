package com.faisal.dev.atsanalyzer.scoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.faisal.dev.atsanalyzer.scoring.config.AtsScoringProperties;
import com.faisal.dev.atsanalyzer.scoring.constants.ScoreCategory;
import com.faisal.dev.atsanalyzer.scoring.detector.ExperienceExtractor;
import com.faisal.dev.atsanalyzer.scoring.detector.ResumeDomainDetector;
import com.faisal.dev.atsanalyzer.scoring.detector.SeniorityDetector;
import com.faisal.dev.atsanalyzer.scoring.domain.DomainPackRegistry;
import com.faisal.dev.atsanalyzer.scoring.domain.DomainRelevanceScorer;
import com.faisal.dev.atsanalyzer.scoring.domain.ResumeDomainIntelligenceEngine;
import com.faisal.dev.atsanalyzer.scoring.engine.AtsScoringEngine;
import com.faisal.dev.atsanalyzer.scoring.engine.ScoringContext;
import com.faisal.dev.atsanalyzer.scoring.result.DetailedScoreResult;
import com.faisal.dev.atsanalyzer.scoring.scorer.ResumeScorer;
import com.faisal.dev.atsanalyzer.scoring.universal.ActionVerbScorer;
import com.faisal.dev.atsanalyzer.scoring.universal.AtsCompatibilityScorer;
import com.faisal.dev.atsanalyzer.scoring.universal.ContentClarityScorer;
import com.faisal.dev.atsanalyzer.scoring.universal.MetricsImpactScorer;
import com.faisal.dev.atsanalyzer.scoring.universal.ReadabilityScorer;
import com.faisal.dev.atsanalyzer.scoring.universal.ResumeLengthScorer;
import com.faisal.dev.atsanalyzer.scoring.universal.StructureQualityScorer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtsScoringEngineTest {

    @Test
    void strongResumeScoresHigherThanWeakResume() {

        AtsScoringEngine engine =
                buildEngine();

        String strongResume = """
                John Doe
                john@example.com | 555-123-4567 | linkedin.com/in/johndoe

                SUMMARY
                Backend engineer with 6 years of experience building reliable APIs and data platforms.

                EXPERIENCE
                - Built Spring Boot services that reduced API latency by 35% across 2 million monthly requests.
                - Optimized MySQL queries and improved reporting job runtime from 18 minutes to 6 minutes.
                - Led release automation with GitHub Actions and cut deployment time by 60%.

                SKILLS
                Java, Spring Boot, SQL, Docker, GitHub Actions, JUnit, AWS

                EDUCATION
                B.Tech Computer Science, 2020
                """;

        String weakResume = """
                Resume

                Worked on many things and helped the team.
                Responsible for software tasks and communication.
                Good person with passion.
                """;

        DetailedScoreResult strongResult =
                engine.calculateScores(buildContext(strongResume));
        DetailedScoreResult weakResult =
                engine.calculateScores(buildContext(weakResume));

        assertTrue(
                strongResult.getOverallScore() >
                        weakResult.getOverallScore()
        );
        assertTrue(
                strongResult.getUniversalScore() >
                        weakResult.getUniversalScore()
        );
        assertTrue(
                strongResult.getImprovements().size() <
                        weakResult.getImprovements().size()
        );
    }

    @Test
    void programManagerResumeMapsToOperationsDomain() {

        AtsScoringEngine engine =
                buildEngine();

        DetailedScoreResult result =
                engine.calculateScores(buildContext("""
                        SUMMARY
                        Senior program manager with 8 years of experience leading cross-functional delivery and transformation workstreams.

                        CORE COMPETENCIES
                        Program Management, Agile, Jira, Confluence, Roadmap, Stakeholder Management, Risk Management, KPI Reporting, Budget Management

                        EXPERIENCE
                        - Led agile program delivery across multiple workstreams and improved release governance for 12 teams.
                        - Drove roadmap alignment, budget reviews, and dependency management across product and operations groups.

                        EDUCATION
                        MBA, 2020
                        """));

        assertEquals(
                "Operations & Program Delivery",
                result.getResumeInsights()
                        .detectedDomainLabel()
        );
        assertTrue(
                result.getDomainRelevanceScore() >= 60.0d
        );
        assertTrue(
                result.getResumeInsights()
                        .detectedSkills()
                        .stream()
                        .anyMatch(skill ->
                                skill.equalsIgnoreCase("Jira")
                        )
        );
    }

    @Test
    void midLevelEngineeringResumeGetsSeniorityAwareMissingSkills() {

        AtsScoringEngine engine =
                buildEngine();

        DetailedScoreResult result =
                engine.calculateScores(buildContext("""
                        Jane Doe
                        jane@example.com | 555-222-3333 | linkedin.com/in/janedoe

                        SUMMARY
                        Software engineer with 4 years of experience building backend APIs and data services.

                        EXPERIENCE
                        - Built Java services and SQL reporting workflows for internal business users.
                        - Improved API response times by 20% and supported production issue resolution.

                        SKILLS
                        Java, SQL, REST API, Git

                        EDUCATION
                        B.E. Computer Science, 2021
                        """));

        assertEquals(
                "Software Engineering",
                result.getResumeInsights()
                        .detectedDomainLabel()
        );
        assertTrue(
                result.getResumeInsights()
                        .missingSkills()
                        .stream()
                        .anyMatch(skill ->
                                skill.equalsIgnoreCase("Docker") ||
                                        skill.equalsIgnoreCase("CI/CD") ||
                                        skill.equalsIgnoreCase("cloud")
                        )
        );
    }

    @Test
    void engineProducesUniversalAndDomainCategories() {

        AtsScoringEngine engine =
                buildEngine();

        DetailedScoreResult result =
                engine.calculateScores(buildContext("""
                        jane@example.com | 555-111-2222

                        EXPERIENCE
                        - Developed reporting workflows and improved reliability by 20%.

                        SKILLS
                        Excel, Jira, Reporting

                        EDUCATION
                        B.E., 2021
                        """));

        assertEquals(8, result.getCategoryScores().size());
        assertTrue(
                result.getCategoryScores()
                        .stream()
                        .map(score -> score.getCategory())
                        .toList()
                        .containsAll(List.of(
                                ScoreCategory.ATS_COMPATIBILITY,
                                ScoreCategory.IMPACT_METRICS,
                                ScoreCategory.READABILITY,
                                ScoreCategory.ACTION_VERBS,
                                ScoreCategory.CONTENT_CLARITY,
                                ScoreCategory.RESUME_LENGTH,
                                ScoreCategory.STRUCTURE_QUALITY,
                                ScoreCategory.DOMAIN_RELEVANCE
                        ))
        );
    }

    private static AtsScoringEngine buildEngine() {

        AtsScoringProperties properties =
                buildProperties();

        DomainPackRegistry registry =
                new DomainPackRegistry(
                        new ObjectMapper()
                );
        ResumeDomainDetector domainDetector =
                new ResumeDomainDetector(registry);
        ResumeDomainIntelligenceEngine intelligenceEngine =
                new ResumeDomainIntelligenceEngine(
                        domainDetector,
                        new ExperienceExtractor(),
                        new SeniorityDetector()
                );

        return new AtsScoringEngine(
                intelligenceEngine,
                buildScorers(properties)
        );
    }

    private static List<ResumeScorer> buildScorers(
            AtsScoringProperties properties
    ) {

        return List.of(
                new AtsCompatibilityScorer(properties),
                new MetricsImpactScorer(properties),
                new ReadabilityScorer(properties),
                new ActionVerbScorer(properties),
                new ContentClarityScorer(properties),
                new ResumeLengthScorer(properties),
                new StructureQualityScorer(properties),
                new DomainRelevanceScorer(properties)
        );
    }

    private static AtsScoringProperties buildProperties() {

        AtsScoringProperties properties =
                new AtsScoringProperties();

        properties.setWeights(Map.of(
                "ats-compatibility", 0.14d,
                "impact-metrics", 0.16d,
                "readability", 0.12d,
                "action-verbs", 0.10d,
                "content-clarity", 0.10d,
                "resume-length", 0.08d,
                "structure-quality", 0.10d,
                "domain-relevance", 0.20d
        ));
        properties.setStandardSections(Map.of(
                "contact", List.of("contact", "email", "phone", "linkedin"),
                "summary", List.of("summary", "profile", "professional summary"),
                "experience", List.of("experience", "work experience"),
                "skills", List.of(
                        "skills",
                        "technical skills",
                        "competencies",
                        "core competencies"
                ),
                "education", List.of("education"),
                "projects", List.of("projects")
        ));
        properties.setActionVerbs(List.of(
                "built",
                "developed",
                "optimized",
                "led",
                "improved",
                "delivered",
                "managed"
        ));
        properties.setWeakActionVerbs(List.of(
                "helped",
                "responsible for",
                "worked on",
                "assisted"
        ));
        properties.setVaguePhrases(List.of(
                "hard working",
                "team player",
                "responsible for",
                "worked on"
        ));

        return properties;
    }

    private static ScoringContext buildContext(
            String cleanedText
    ) {

        return ScoringContext.of(
                cleanedText,
                cleanedText,
                cleanedText.trim().split("\\s+").length,
                cleanedText.length()
        );
    }
}
