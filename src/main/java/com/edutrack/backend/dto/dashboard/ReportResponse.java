package com.edutrack.backend.dto.dashboard;

import java.util.List;

import com.edutrack.backend.dto.student.StudentResponse;

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
public class ReportResponse {
    private ClassSummaryResponse summary;
    private List<StudentResponse> toppers;
    private List<StudentResponse> attentionNeeded;
}
