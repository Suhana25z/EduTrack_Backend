package com.edutrack.backend.dto.dashboard;

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
public class ClassSummaryResponse {
    private int totalStudents;
    private double classAverage;
    private double highestAverage;
    private double lowestAverage;
    private long aboveAverageCount;
    private long needsAttentionCount;
}
