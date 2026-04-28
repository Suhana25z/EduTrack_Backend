package com.edutrack.backend.dto.student;

import java.time.LocalDateTime;
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
public class StudentResponse {
    private Long id;
    private String studentId;
    private String name;
    private String branch;
    private String email;
    private LocalDateTime joinedAt;
    private Double average;
    private Integer total;
    private List<SubjectMarkResponse> marks;
}
