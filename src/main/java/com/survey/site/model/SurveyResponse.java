////
////
////package com.survey.site.model;
////
////import jakarta.persistence.*;
////import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
////
////@Entity
////@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
////public class SurveyResponse {
////
////    @Id
////    @GeneratedValue(strategy = GenerationType.IDENTITY)
////    private Long id;
////
////    private String answer;
////    private String remarks;
////    private String imageUrl;
////
////    // ✅ RELATION WITH SPACE
////    @ManyToOne(fetch = FetchType.LAZY)
////    @JoinColumn(name = "space_id", nullable = false)
////    private Space space;
////
////    // ✅ RELATION WITH QUESTION
////    @ManyToOne(fetch = FetchType.LAZY)
////    @JoinColumn(name = "question_id", nullable = false)
////    private Question question;
////
////    public SurveyResponse() {}
////
////    // GETTERS
////    public Long getId() { return id; }
////    public String getAnswer() { return answer; }
////    public String getRemarks() { return remarks; }
////    public String getImageUrl() { return imageUrl; }
////    public Space getSpace() { return space; }
////    public Question getQuestion() { return question; }
////
////    // SETTERS
////    public void setAnswer(String answer) { this.answer = answer; }
////    public void setRemarks(String remarks) { this.remarks = remarks; }
////    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
////    public void setSpace(Space space) { this.space = space; }
////    public void setQuestion(Question question) { this.question = question; }
////}
//
//package com.survey.site.model;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import jakarta.persistence.*;
//
//@Entity
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
//public class SurveyResponse {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String answer;
//    private String remarks;
//    private String imageUrl;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "space_id")
//    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "floor", "engineer"})
//    private Space space;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "question_id")
//    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "template"})
//    private Question question;
//
//    public SurveyResponse() {}
//
//    public Long getId() {
//        return id;
//    }
//
//    public String getAnswer() {
//        return answer;
//    }
//
//    public String getRemarks() {
//        return remarks;
//    }
//
//    public String getImageUrl() {
//        return imageUrl;
//    }
//
//    public Space getSpace() {
//        return space;
//    }
//
//    public Question getQuestion() {
//        return question;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public void setAnswer(String answer) {
//        this.answer = answer;
//    }
//
//    public void setRemarks(String remarks) {
//        this.remarks = remarks;
//    }
//
//    public void setImageUrl(String imageUrl) {
//        this.imageUrl = imageUrl;
//    }
//
//    public void setSpace(Space space) {
//        this.space = space;
//    }
//
//    public void setQuestion(Question question) {
//        this.question = question;
//    }
//}

package com.survey.site.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SurveyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String answer;
    private String remarks;
    private String imageUrl;

    // ✅ RF FIELDS
    private Integer rfStrength;
    private String rfStatus;
    private String rfNotes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "space_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "floor", "engineer"})
    private Space space;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "template"})
    private Question question;

    public SurveyResponse() {}

    // ================= GETTERS =================
    public Long getId() { return id; }
    public String getAnswer() { return answer; }
    public String getRemarks() { return remarks; }
    public String getImageUrl() { return imageUrl; }
    public Integer getRfStrength() { return rfStrength; }
    public String getRfStatus() { return rfStatus; }
    public String getRfNotes() { return rfNotes; }
    public Space getSpace() { return space; }
    public Question getQuestion() { return question; }

    // ================= SETTERS =================
    public void setId(Long id) { this.id = id; }
    public void setAnswer(String answer) { this.answer = answer; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setRfStrength(Integer rfStrength) { this.rfStrength = rfStrength; }
    public void setRfStatus(String rfStatus) { this.rfStatus = rfStatus; }
    public void setRfNotes(String rfNotes) { this.rfNotes = rfNotes; }
    public void setSpace(Space space) { this.space = space; }
    public void setQuestion(Question question) { this.question = question; }
}