//package com.survey.site.repository;
//
//import com.survey.site.model.Floorplan;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public interface FloorplanRepository extends JpaRepository<Floorplan, Long> {
//    // Optional: find floorplan by floor
//    Floorplan findByFloorId(Long floorId);
//}

//package com.survey.site.repository;
//
//import com.survey.site.model.Floorplan;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface FloorplanRepository extends JpaRepository<Floorplan, Long> {
//    // Get all floorplans for a specific floor
//    List<Floorplan> findByFloor_Id(Long floorId);
//
//    // ✅ Corrected: Get all floorplans for a property via floor → property
//    List<Floorplan> findByFloor_Property_Id(Long propertyId);
//}

//
//package com.survey.site.repository;
//
//import com.survey.site.model.Floorplan;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface FloorplanRepository extends JpaRepository<Floorplan, Long> {
//
//    List<Floorplan> findByFloor_Id(Long floorId);
//
//    // Custom query to get floorplans by property
//    @Query("SELECT fp FROM Floorplan fp JOIN fp.floor f WHERE f.property.id = :propertyId")
//    List<Floorplan> findByPropertyId(@Param("propertyId") Long propertyId);
//
//}

package com.survey.site.repository;

import com.survey.site.model.Floorplan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface FloorplanRepository extends JpaRepository<Floorplan, Long> {
    Optional<Floorplan> findByFloorId(Long floorId);

    List<Floorplan> findByFloor_Building_Property_Id(Long propertyId);
}