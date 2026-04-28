package com.faisal.dev.atsanalyzer.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "suggestions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Suggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resume resume;

    @Column(name = "category")
    private String category;

    @Column(name = "severity")
    private String severity;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime createdAt;
}