package com.survey.site.controller;

import com.survey.site.model.SignalStrength;
import com.survey.site.repository.SignalStrengthRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/signals")
@CrossOrigin("*")
public class SignalController {

    @Autowired
    private SignalStrengthRepository signalRepository;

    @PostMapping
    public SignalStrength saveSignal(@RequestBody SignalStrength signal){
        return signalRepository.save(signal);
    }
}