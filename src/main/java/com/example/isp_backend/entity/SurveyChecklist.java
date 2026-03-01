package com.example.isp_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "survey_checklists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status; // DRAFT, SUBMITTED

    private LocalDateTime createdAt;

    // Attach to Floor OR Space
    @ManyToOne
    @JoinColumn(name = "floor_id")
    private Floor floor;

    @ManyToOne
    @JoinColumn(name = "space_id")
    private Space space;

    @OneToMany(mappedBy = "surveyChecklist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyResponse> responses;
}