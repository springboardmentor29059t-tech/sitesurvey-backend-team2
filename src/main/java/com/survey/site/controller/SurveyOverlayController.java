package com.survey.site.controller;

import com.survey.site.model.Space;
import com.survey.site.model.SurveyResponse;
import com.survey.site.repository.SpaceRepository;
import com.survey.site.repository.SurveyResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/overlay")
@CrossOrigin("*")
@RequiredArgsConstructor
public class SurveyOverlayController {

    private final SpaceRepository spaceRepository;
    private final SurveyResponseRepository surveyResponseRepository;

    @GetMapping("/floor/{floorId}")
    public List<Map<String, Object>> getFloorOverlay(@PathVariable Long floorId) {

        List<Space> spaces = spaceRepository.findByFloorId(floorId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Space space : spaces) {

            List<SurveyResponse> responses =
                    surveyResponseRepository.findBySpaceId(space.getId());

            String color = "gray";

            for (SurveyResponse response : responses) {
                String ans = response.getAnswer();

                if (ans == null) continue;

                ans = ans.toLowerCase();

                if (ans.contains("-4") || ans.contains("good") || ans.contains("yes")) {
                    color = "green";
                    break;
                } else if (ans.contains("-6") || ans.contains("medium")) {
                    color = "yellow";
                } else if (ans.contains("-7") || ans.contains("weak") || ans.contains("no")) {
                    color = "red";
                }
            }

            Map<String, Object> row = new HashMap<>();
            row.put("spaceId", space.getId());
            row.put("spaceName", space.getName());
            row.put("color", color);

            result.add(row);
        }

        return result;
    }
}