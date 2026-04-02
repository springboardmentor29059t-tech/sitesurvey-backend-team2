

package com.survey.site.service;

import com.survey.site.model.Floor;
import com.survey.site.model.Space;
import com.survey.site.model.SurveyResponse;
import com.survey.site.repository.FloorRepository;
import com.survey.site.repository.SpaceRepository;
import com.survey.site.repository.SurveyResponseRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.List;

@Service
public class PdfService {

    @Autowired
    private FloorRepository floorRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private SurveyResponseRepository surveyRepo;

    public byte[] generateFloorPdf(Long floorId) {

        try {

            Floor floor = floorRepository.findById(floorId).orElse(null);
            if (floor == null) return null;

            List<Space> spaces = spaceRepository.findByFloorId(floorId);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);

            document.open();


            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Floor Survey Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Floor: " + floor.getName()));
            document.add(new Paragraph("Floor Number: " + floor.getFloorNumber()));
            document.add(new Paragraph(" "));


            for (Space space : spaces) {

                document.add(new Paragraph("--------------------------------------------------"));

                Font spaceFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
                document.add(new Paragraph("Space: " + space.getName(), spaceFont));
                document.add(new Paragraph("Area: " + space.getArea()));
                document.add(new Paragraph(" "));


                List<SurveyResponse> responses =
                        surveyRepo.findBySpaceId(space.getId());

                if (responses.isEmpty()) {
                    document.add(new Paragraph("No survey submitted"));
                    continue;
                }


                for (SurveyResponse r : responses) {


                    String questionText = "N/A";

                    if (r.getQuestion() != null) {
                        if (r.getQuestion().getQuestion() != null) {
                            questionText = r.getQuestion().getQuestion();
                        } else if (r.getQuestion().getText() != null) {
                            questionText = r.getQuestion().getText();
                        }
                    }

                    document.add(new Paragraph("Q: " + questionText));
                    document.add(new Paragraph("Answer: " + r.getAnswer()));
                    document.add(new Paragraph("Remarks: " + r.getRemarks()));


                    if (r.getImageUrl() != null && !r.getImageUrl().isEmpty()) {
                        try {
                            Image img = Image.getInstance(new URL(r.getImageUrl()));
                            img.scaleToFit(250, 250);
                            document.add(img);
                        } catch (Exception e) {
                            document.add(new Paragraph("Image load failed"));
                        }
                    }

                    document.add(new Paragraph(" "));
                }
            }

            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}