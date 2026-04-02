
package com.survey.site.model;

import jakarta.persistence.*;

@Entity
public class SurveyPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contentType;

//    @Lob
//    private byte[] data;

    @Lob
    @Column(name = "data", columnDefinition = "LONGBLOB")
    private byte[] data;

    @ManyToOne
    @JoinColumn(name = "survey_id")
    private Survey survey;

    public SurveyPhoto() {}

    public Long getId() {
        return id;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getData() {
        return data;
    }

    public Survey getSurvey() {
        return survey;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

}