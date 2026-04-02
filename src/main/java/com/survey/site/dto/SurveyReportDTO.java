package com.survey.site.dto;

public class SurveyReportDTO {

    private Long total;
    private Long submitted;
    private Long pending;

    public SurveyReportDTO(Long total, Long submitted, Long pending) {
        this.total = total;
        this.submitted = submitted;
        this.pending = pending;
    }

    public Long getTotal() {
        return total;
    }

    public Long getSubmitted() {
        return submitted;
    }

    public Long getPending() {
        return pending;
    }
}