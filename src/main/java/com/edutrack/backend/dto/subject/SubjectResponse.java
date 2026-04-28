package com.edutrack.backend.dto.subject;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubjectResponse {

    private Long id;
    private String name;
    private Integer subjectOrder;
}
