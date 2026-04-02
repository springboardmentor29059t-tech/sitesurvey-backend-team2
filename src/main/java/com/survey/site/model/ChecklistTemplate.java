
package com.survey.site.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class ChecklistTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String spaceType;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;

    // 🔹 SET TEMPLATE REFERENCE
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
        if (questions != null) {
            for (Question q : questions) {
                q.setTemplate(this);
            }
        }
    }

    // ✅ GETTERS & SETTERS

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpaceType() {
        return spaceType;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSpaceType(String spaceType) {
        this.spaceType = spaceType;
    }
}