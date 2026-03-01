package com.example.isp_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "checklist_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;

    private String type; // TEXT, YES_NO, NUMBER

    @ManyToOne
    @JoinColumn(name = "template_id")
    @JsonIgnore
    private ChecklistTemplate template;
}