package com.survey.site.controller;

import com.survey.site.model.Question;
import com.survey.site.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/questions")
@CrossOrigin("*")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    // create RF question
    @PostMapping("/add")
    public Question addQuestion(@RequestBody Question question) {
        return questionService.saveQuestion(question);
    }

    // get only RF questions
    @GetMapping("/rf")
    public List<Question> getRfQuestions() {
        return questionService.getRfQuestions();
    }

    // all questions
    @GetMapping("/all")
    public List<Question> getAllQuestions() {
        return questionService.getAllQuestions();
    }
}