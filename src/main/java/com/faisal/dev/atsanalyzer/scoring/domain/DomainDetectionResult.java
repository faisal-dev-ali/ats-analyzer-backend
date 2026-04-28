package com.faisal.dev.atsanalyzer.scoring.domain;

import com.faisal.dev.atsanalyzer.scoring.config.DomainPackDefinition;

import java.util.List;

public record DomainDetectionResult(
        DomainPackDefinition domainPack,
        double confidence,
        List<String> matchedSignals
) {
}
