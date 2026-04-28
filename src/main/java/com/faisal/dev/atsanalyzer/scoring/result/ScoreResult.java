package com.faisal.dev.atsanalyzer.scoring.result;

import com.faisal.dev.atsanalyzer.scoring.constants.ScoreCategory;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ScoreResult {

    private ScoreCategory category;

    private Double score;

    private Double weight;

    private String explanation;

    @Builder.Default
    private List<String> highlights = List.of();

    @Builder.Default
    private List<String> recommendations = List.of();
}
