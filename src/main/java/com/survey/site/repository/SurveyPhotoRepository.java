//////package com.survey.site.repository;
//////
//////import com.survey.site.model.SurveyPhoto;
//////import org.springframework.data.jpa.repository.JpaRepository;
//////
//////import java.util.List;
//////
//////public interface SurveyPhotoRepository extends JpaRepository<SurveyPhoto, Long> {
//////
//////    List<SurveyPhoto> findBySpaceId(Long spaceId);
//////
//////}
////
////package com.survey.site.repository;
////
////import com.survey.site.model.SurveyPhoto;
////import org.springframework.data.jpa.repository.JpaRepository;
////
////import java.util.List;
////
////public interface SurveyPhotoRepository extends JpaRepository<SurveyPhoto, Long> {
////    List<SurveyPhoto> findBySpaceId(Long spaceId);
////}
//
//package com.survey.site.repository;
//
//import com.survey.site.model.Property;
//import com.survey.site.model.SurveyPhoto;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//
//public interface SurveyPhotoRepository extends JpaRepository<SurveyPhoto, Long> {
//
//
//
//
//
//    List<SurveyPhoto> findBySpaceId(Long spaceId);
//}

package com.survey.site.repository;

import com.survey.site.model.SurveyPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SurveyPhotoRepository extends JpaRepository<SurveyPhoto, Long> {

    List<SurveyPhoto> findBySurveyId(Long surveyId);


}