package com.edutrack.backend.dto.dashboard;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    private List<String> overallSuggestions;
    private List<SubjectRecommendation> subjectSpecific;
    private List<String> trends;
    private List<String> nextSteps;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectRecommendation {
        private String subjectName;
        private int currentScore;
        private List<String> strategies;
    }
}
