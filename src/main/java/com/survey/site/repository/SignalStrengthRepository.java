
package com.survey.site.repository;

import com.survey.site.model.SignalStrength;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SignalStrengthRepository extends JpaRepository<SignalStrength, Long> {

    List<SignalStrength> findByEngineerId(Long engineerId);

}