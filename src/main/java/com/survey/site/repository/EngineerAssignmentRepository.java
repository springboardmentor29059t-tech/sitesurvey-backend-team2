package com.survey.site.repository;

import com.survey.site.model.EngineerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EngineerAssignmentRepository
        extends JpaRepository<EngineerAssignment,Long>{

    List<EngineerAssignment> findByEngineerId(Long engineerId);

}