package com.edutrack.backend.dto.dashboard;

import java.util.List;

import com.edutrack.backend.dto.student.SubjectMarkResponse;

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
public class StudentDashboardResponse {
    private String studentId;
    private String studentName;
    private String branch;
    private int total;
    private double average;
    private int highest;
    private int lowest;
    private double classAverage;
    private int consistencyScore;
    private String performanceLevel;
    private String trend;
    private List<SubjectMarkResponse> marks;
    private List<InsightResponse> strengths;
    private List<InsightResponse> weaknesses;
}
