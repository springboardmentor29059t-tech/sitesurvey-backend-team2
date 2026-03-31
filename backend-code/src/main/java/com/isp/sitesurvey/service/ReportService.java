package com.isp.sitesurvey.service;

import com.isp.sitesurvey.entity.*;
import com.isp.sitesurvey.repository.*;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class ReportService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private SurveyResponseRepository surveyResponseRepository;

    @Autowired
    private RfSignalDataRepository rfSignalDataRepository;

    public byte[] generatePropertyReport(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Title
        document.add(new Paragraph("Site Survey Report: " + property.getName()).setFontSize(20).setBold());
        document.add(new Paragraph("Address: " + property.getAddressLine1() + ", " + property.getCity()));
        document.add(new Paragraph("\n"));

        // Table for Survey Responses
        float[] columnWidths = {2, 1, 1, 2, 4};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.useAllAvailableWidth();

        table.addHeaderCell("Space Name");
        table.addHeaderCell("Power");
        table.addHeaderCell("Cooling");
        table.addHeaderCell("Signal (dBm)");
        table.addHeaderCell("Notes");

        for (Building b : property.getBuildings()) {
            for (Floor f : b.getFloors()) {
                for (Space s : f.getSpaces()) {
                    SurveyResponse response = surveyResponseRepository.findBySpaceId(s.getId()).orElse(null);
                    if (response != null) {
                        table.addCell(s.getName());
                        table.addCell(response.getPowerAvailable() ? "Yes" : "No");
                        table.addCell(response.getCoolingAvailable() ? "Yes" : "No");
                        table.addCell(String.valueOf(response.getSignalStrengthDbm()));
                        table.addCell(response.getAdditionalNotes() != null ? response.getAdditionalNotes() : "");
                    } else {
                        table.addCell(s.getName());
                        table.addCell("N/A");
                        table.addCell("N/A");
                        table.addCell("N/A");
                        table.addCell("Incomplete");
                    }
                }
            }
        }

        document.add(table);

        // --- RF SIGNAL ANALYSIS SECTION ---
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("RF Signal Analysis Inventory").setFontSize(16).setBold().setUnderline());
        
        java.util.List<RfSignalData> signals = rfSignalDataRepository.findByPropertyId(propertyId);
        if (signals == null || signals.isEmpty()) {
            document.add(new Paragraph("No integrated RF signal data (Vistumbler, Kismet, or SPLAT!) detected for this site."));
        } else {
            document.add(new Paragraph("Total RF Samples Captured: " + signals.size()).setFontSize(10).setItalic());
            
            float[] rfWidths = {3, 3, 2, 4};
            Table rfTable = new Table(UnitValue.createPercentArray(rfWidths));
            rfTable.useAllAvailableWidth();
            rfTable.setMarginTop(10);

            rfTable.addHeaderCell("SSID");
            rfTable.addHeaderCell("BSSID");
            rfTable.addHeaderCell("Signal (dBm)");
            rfTable.addHeaderCell("GPS Location (Lat, Lon)");

            for (RfSignalData sig : signals) {
                rfTable.addCell(sig.getSsid() != null ? sig.getSsid() : "Unknown");
                rfTable.addCell(sig.getBssid() != null ? sig.getBssid() : "N/A");
                
                String strengthText = sig.getSignalStrength() + " dBm";
                rfTable.addCell(strengthText);
                
                String location = String.format("%.6f, %.6f", 
                    sig.getLatitude() != null ? sig.getLatitude() : 0.0, 
                    sig.getLongitude() != null ? sig.getLongitude() : 0.0);
                rfTable.addCell(location);
            }
            document.add(rfTable);
        }
        document.close();
        return baos.toByteArray();
    }

    public byte[] generateRfReport(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("RF Signal Survey Report").setFontSize(22).setBold().setUnderline());
        document.add(new Paragraph("Property: " + property.getName()).setBold());
        document.add(new Paragraph("Address: " + property.getAddressLine1() + ", " + property.getCity()));
        document.add(new Paragraph("\n"));

        java.util.List<RfSignalData> signals = rfSignalDataRepository.findByPropertyId(propertyId);
        if (signals == null || signals.isEmpty()) {
            document.add(new Paragraph("No RF signal data available for this property."));
        } else {
            document.add(new Paragraph("Integrated Wireless Signal Data (Vistumbler/Kismet/SPLAT!)").setItalic());
            document.add(new Paragraph("Total Samples: " + signals.size()).setFontSize(10));

            float[] rfWidths = {2, 2, 2, 4};
            Table rfTable = new Table(UnitValue.createPercentArray(rfWidths));
            rfTable.useAllAvailableWidth();
            rfTable.setMarginTop(10);

            rfTable.addHeaderCell("SSID");
            rfTable.addHeaderCell("BSSID");
            rfTable.addHeaderCell("Signal (dBm)");
            rfTable.addHeaderCell("GPS Location");

            for (RfSignalData sig : signals) {
                rfTable.addCell(sig.getSsid() != null ? sig.getSsid() : "Unknown");
                rfTable.addCell(sig.getBssid() != null ? sig.getBssid() : "N/A");
                rfTable.addCell(sig.getSignalStrength() + " dBm");
                rfTable.addCell(String.format("%.6f, %.6f", sig.getLatitude(), sig.getLongitude()));
            }
            document.add(rfTable);
        }

        document.close();
        return baos.toByteArray();
    }
}
