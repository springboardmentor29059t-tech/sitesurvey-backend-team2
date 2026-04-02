package com.survey.site.service;

import com.survey.site.model.Question;
import com.survey.site.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    public Question saveQuestion(Question question) {
        return questionRepository.save(question);
    }

    public List<Question> getRfQuestions() {
        return questionRepository.findByCategory("RF");
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }
}