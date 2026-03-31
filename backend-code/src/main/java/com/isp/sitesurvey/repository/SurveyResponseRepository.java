package com.isp.sitesurvey.repository;

import com.isp.sitesurvey.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
    @Query("SELECT s FROM SurveyResponse s WHERE s.space.id = :spaceId")
    Optional<SurveyResponse> findBySpaceId(@Param("spaceId") Long spaceId);
}
