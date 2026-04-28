package com.edutrack.backend.repository;

import java.util.List;

import com.edutrack.backend.entity.TeacherSubject;
import com.edutrack.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherSubjectRepository extends JpaRepository<TeacherSubject, Long> {

    List<TeacherSubject> findByTeacherOrderBySubjectOrderAsc(User teacher);

    boolean existsByTeacherAndNameIgnoreCase(User teacher, String name);
}
