package com.isp.sitesurvey.repository;

import com.isp.sitesurvey.entity.RfSignalData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RfSignalDataRepository extends JpaRepository<RfSignalData, Long> {
    List<RfSignalData> findByPropertyId(Long propertyId);
}
