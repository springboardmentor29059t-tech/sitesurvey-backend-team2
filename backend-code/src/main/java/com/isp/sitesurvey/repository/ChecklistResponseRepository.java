package com.isp.sitesurvey.repository;

import com.isp.sitesurvey.entity.ChecklistResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChecklistResponseRepository extends JpaRepository<ChecklistResponse, Long> {
}
