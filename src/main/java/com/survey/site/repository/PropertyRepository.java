

package com.survey.site.repository;

import com.survey.site.model.Property;
import com.survey.site.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    // Properties assigned to a specific engineer
    List<Property> findByEngineer(User engineer);

    // Count properties assigned to a specific engineer
    Long countByEngineer(User engineer);

    // Optional: use engineer ID
    List<Property> findByEngineerId(Long engineerId);
    Long countByEngineerId(Long engineerId);

    // Use email if needed
    List<Property> findByEngineerEmail(String email);
}