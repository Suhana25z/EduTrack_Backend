package com.edutrack.backend.service;

import com.edutrack.backend.repository.StudentRepository;
import com.edutrack.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .or(() -> studentRepository.findByStudentId(username).map(student -> student.getUser()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
