package com.faisal.dev.atsanalyzer.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalFileName;

    private String storedFileName;

    private String fileType;

    private Long fileSize;

    private String storagePath;

    private String processingStatus;

    private LocalDateTime uploadedAt;

    private LocalDateTime processedAt;

    @Column(length = 500)
    private String failureReason;
}
