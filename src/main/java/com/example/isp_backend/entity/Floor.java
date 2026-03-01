package com.example.isp_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "floors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Floor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer floorNumber;

    private String floorPlanPath;

    private LocalDateTime createdAt;

    // 🔥 Prevent recursion
    @ManyToOne
    @JoinColumn(name = "building_id", nullable = false)
    @JsonIgnore
    private Building building;

    @OneToMany(mappedBy = "floor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Space> spaces;
}