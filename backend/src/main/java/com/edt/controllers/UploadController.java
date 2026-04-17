package com.edt.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "http://localhost:4200")
public class UploadController {

    @Value("${upload.path:./uploads}")
    private String uploadPath;
    
    @Value("${server.port:8080}")
    private String serverPort;

    @PostMapping("/logo")
    public ResponseEntity<?> uploadLogo(@RequestParam("file") MultipartFile file) {
        try {
            // Créer le dossier uploads s'il n'existe pas
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Générer un nom unique pour le fichier
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadPath, fileName);

            // Sauvegarder le fichier
            Files.write(filePath, file.getBytes());

            // Retourner l'URL complète du fichier
            String fileUrl = "http://localhost:" + serverPort + "/uploads/" + fileName;
            
            Map<String, String> response = new HashMap<>();
            response.put("logoUrl", fileUrl);
            
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}