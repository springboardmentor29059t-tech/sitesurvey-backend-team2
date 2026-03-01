package com.example.isp_backend.controller;

import com.example.isp_backend.entity.Property;
import com.example.isp_backend.entity.Client;
import com.example.isp_backend.repository.PropertyRepository;
import com.example.isp_backend.repository.ClientRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.io.*;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class PropertyController {

    private final PropertyRepository propertyRepository;
    private final ClientRepository clientRepository;

    // ===============================
    // Create Property
    // ===============================
    @PostMapping
    public Property createProperty(@RequestBody Property property) {

        property.setCreatedAt(LocalDateTime.now());
        property.setImagePath(null);

        return propertyRepository.save(property);
    }

    // ===============================
    // Get All Properties
    // ===============================
    @GetMapping
    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    // ===============================
    // Upload Image
    // ===============================
    @PostMapping("/{id}/upload")
    public Property uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        String uploadDir = System.getProperty("user.dir")
                + File.separator + "uploads" + File.separator;

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File destination = new File(uploadDir + fileName);

        file.transferTo(destination);

        property.setImagePath("uploads/" + fileName);

        return propertyRepository.save(property);
    }

    // ===============================
    // Upload CSV
    // ===============================
    @PostMapping("/upload-csv")
    public String uploadCSV(@RequestParam("file") MultipartFile file) {

        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream()));

            reader.readLine(); // skip header

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                String[] data = line.split(",");

                Client client = new Client();
                client.setName(data[0].trim());
                client.setEmail(data[1].trim());
                client.setPhone(data[2].trim());

                clientRepository.save(client);
            }

            return "CSV Uploaded Successfully";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error Uploading CSV";
        }
    }

    // ===============================
    // Get All Clients
    // ===============================
    @GetMapping("/clients")
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    // ===============================
    // Delete Client
    // ===============================
    @DeleteMapping("/clients/{id}")
    public String deleteClient(@PathVariable Long id) {

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        clientRepository.delete(client);

        return "Client deleted successfully";
    }

    // ===============================
    // Get Property By ID
    // ===============================
    @GetMapping("/{id}")
    public Property getPropertyById(@PathVariable Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));
    }

    // ===============================
    // Delete Property
    // ===============================
    @DeleteMapping("/{id}")
    public String deleteProperty(@PathVariable Long id) {

        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        propertyRepository.delete(property);

        return "Property deleted successfully";
    }
}