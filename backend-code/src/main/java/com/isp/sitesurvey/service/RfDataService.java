package com.isp.sitesurvey.service;

import com.isp.sitesurvey.entity.Property;
import com.isp.sitesurvey.entity.RfSignalData;
import com.isp.sitesurvey.repository.PropertyRepository;
import com.isp.sitesurvey.repository.RfSignalDataRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Service
public class RfDataService {

    @Autowired
    private RfSignalDataRepository rfSignalDataRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    public List<RfSignalData> parseAndSaveVistumblerCsv(Long propertyId, MultipartFile file) throws Exception {
        return parseCsv(propertyId, file, "Vistumbler");
    }

    public List<RfSignalData> parseAndSaveKismetCsv(Long propertyId, MultipartFile file) throws Exception {
        return parseCsv(propertyId, file, "Kismet");
    }

    private List<RfSignalData> parseCsv(Long propertyId, MultipartFile file, String sourceTool) throws Exception {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        List<RfSignalData> signals = new ArrayList<>();
        
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreHeaderCase()
                    .withTrim());

            for (CSVRecord record : csvParser) {
                try {
                    RfSignalData signal = new RfSignalData();
                    signal.setProperty(property);
                    
                    // SSID / BSSID
                    String ssid = getVal(record, "SSID", "AP Name", "ESSID");
                    String bssid = getVal(record, "BSSID", "MAC", "Address", "AP MAC");
                    
                    signal.setSsid(ssid != null ? ssid : "Unknown");
                    signal.setBssid(bssid != null ? bssid : "00:00:00:00:00:00");
                    
                    // Signal Strength (dBm)
                    String sig = getVal(record, "Signal", "Signal_Level", "RSSI", "Strength", "Level");
                    if (sig != null) {
                        try {
                            String numeric = sig.replaceAll("[^-\\d]", "");
                            signal.setSignalStrength(numeric.isEmpty() ? -100 : Integer.parseInt(numeric));
                        } catch (Exception e) { signal.setSignalStrength(-100); }
                    } else {
                        signal.setSignalStrength(-100);
                    }
                    
                    // Coordinates
                    String lat = getVal(record, "Latitude", "LAT", "GPSLat", "GPS Latitude", "Y");
                    String lon = getVal(record, "Longitude", "LON", "GPSLon", "GPS Longitude", "X");

                    if (lat != null && lon != null) {
                        signal.setLatitude(Double.parseDouble(lat));
                        signal.setLongitude(Double.parseDouble(lon));
                    } else {
                        // Skip records without GPS data (common in war-driving logs)
                        continue;
                    }

                    signal.setSourceTool(sourceTool);
                    signals.add(signal);
                } catch (Exception e) {
                    // Skip malformed rows
                    System.err.println("Skipping RF row: " + e.getMessage());
                }
            }
        }
        return rfSignalDataRepository.saveAll(signals);
    }

    private String getVal(CSVRecord rec, String... keys) {
        for (String k : keys) {
            if (rec.isMapped(k) && rec.get(k) != null && !rec.get(k).trim().isEmpty()) {
                return rec.get(k).trim();
            }
        }
        return null;
    }

    public List<RfSignalData> parseAndSaveSplatData(Long propertyId, MultipartFile file) throws Exception {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        List<RfSignalData> signals = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // SPLAT! often uses Space/Tab separated values: Lat Lon Power
                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    try {
                        RfSignalData signal = new RfSignalData();
                        signal.setProperty(property);
                        signal.setLatitude(Double.parseDouble(parts[0]));
                        signal.setLongitude(Double.parseDouble(parts[1]));
                        signal.setSignalStrength(Integer.parseInt(parts[2]));
                        signal.setSsid("SPLAT_PREDICTION");
                        signal.setBssid("00:00:00:00:00:00");
                        signal.setSourceTool("SPLAT!");
                        signals.add(signal);
                    } catch (NumberFormatException e) {
                        // Skip header or malformed lines
                    }
                }
            }
        }
        return rfSignalDataRepository.saveAll(signals);
    }

    public String exportRfDataAsCsv(Long propertyId) {
        List<RfSignalData> signals = rfSignalDataRepository.findByPropertyId(propertyId);
        StringBuilder csv = new StringBuilder("SSID,BSSID,Signal,Latitude,Longitude,SourceTool\n");
        for (RfSignalData s : signals) {
            csv.append(String.format("%s,%s,%d,%f,%f,%s\n",
                s.getSsid(), s.getBssid(), s.getSignalStrength(),
                s.getLatitude(), s.getLongitude(), s.getSourceTool()));
        }
        return csv.toString();
    }

    public List<RfSignalData> getSignalsByProperty(Long propertyId) {
        return rfSignalDataRepository.findByPropertyId(propertyId);
    }
}
