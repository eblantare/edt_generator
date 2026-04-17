// C:\projets\java\edt-generator\backend\src\main\java\com\edt\controllers\ExportController.java
package com.edt.controllers;

import com.edt.entities.CreneauHoraire;
import com.edt.services.GenerationService;
import com.edt.services.ExportPDFMatricielService;
import com.edt.services.EmploiDuTempsMatricielService;
import com.edt.services.ExportEnseignantFormatteService;
import com.edt.repository.CreneauHoraireRepository;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Font;
import com.lowagie.text.Element;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.awt.Color;
import java.util.stream.Collectors;
import com.edt.services.ExportEnseignantService;
import com.edt.entities.Enseignant;
import com.edt.repository.EnseignantRepository;

@RestController
@RequestMapping("/api/export")
@CrossOrigin(origins = "http://localhost:4200")
public class ExportController {
    
    @Autowired
    private GenerationService generationService;
    @Autowired
    private ExportEnseignantService exportEnseignantService;
    @Autowired
    private CreneauHoraireRepository creneauHoraireRepository;
    
    @Autowired
    private ExportPDFMatricielService exportPDFMatricielService;
    
    @Autowired
    private EmploiDuTempsMatricielService emploiDuTempsMatricielService;

    @Autowired
    private EnseignantRepository enseignantRepository;

    @Autowired
    private ExportEnseignantFormatteService exportEnseignantFormatteService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FILENAME_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    /**
     * EXPORT PDF PROFESSIONNEL - Format tableau matriciel
     * C'est CETTE méthode qu'il faut utiliser !
     */
    @GetMapping("/pdf-matriciel/{emploiId}/{classeNom}")
    public ResponseEntity<byte[]> exporterPDFMatriciel(
            @PathVariable String emploiId,
            @PathVariable String classeNom) {
        try {
            System.out.println("📊 Export PDF MATRICIEL pour classe: " + classeNom);
            
            byte[] pdfContent = exportPDFMatricielService.genererPDFMatriciel(emploiId, classeNom);
            
            String filename = "EDT_" + classeNom.replace(" ", "_") + 
                             "_" + LocalDate.now().format(FILENAME_DATE_FORMATTER) + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContent);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur export PDF matriciel: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * EXPORT PDF - Ancien format (linéaire) - CORRIGÉ pour utiliser les VRAIES données
     */
    @GetMapping("/pdf/{emploiId}")
    public ResponseEntity<byte[]> exporterPDF(@PathVariable String emploiId) {
        try {
            System.out.println("📄 Export PDF demandé pour l'emploi: " + emploiId);
            
            byte[] pdfContent = genererPDF(emploiId);
            
            String filename = "emploi-du-temps-" + emploiId + "-" + 
                             LocalDate.now().format(FILENAME_DATE_FORMATTER) + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContent);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur export PDF: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * EXPORT EXCEL - CORRIGÉ pour utiliser les VRAIES données
     */
    @GetMapping("/excel/{emploiId}")
    public ResponseEntity<byte[]> exporterExcel(@PathVariable String emploiId) {
        try {
            System.out.println("📊 Export Excel demandé pour l'emploi: " + emploiId);
            
            byte[] excelContent = genererExcel(emploiId);
            
            String filename = "emploi-du-temps-" + emploiId + "-" + 
                             LocalDate.now().format(FILENAME_DATE_FORMATTER) + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(excelContent);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur export Excel: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * API pour récupérer la matrice JSON (utile pour le frontend)
     */
    @GetMapping("/matrice/{emploiId}/{classeNom}")
    public ResponseEntity<Map<String, Object>> getMatriceEmploiDuTemps(
            @PathVariable String emploiId,
            @PathVariable String classeNom) {
        try {
            Map<String, Object> matrice = emploiDuTempsMatricielService
                .construireMatricePourClasse(emploiId, classeNom);
            return ResponseEntity.ok(matrice);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Génère un PDF avec les VRAIES données de la base
     */
    private byte[] genererPDF(String emploiId) throws Exception {
        // Récupérer les VRAIS créneaux depuis la base de données
        List<CreneauHoraire> creneauxReels = creneauHoraireRepository
            .findByEmploiDuTempsId(emploiId)
            .stream()
            .filter(c -> !c.isEstLibre())  // Uniquement les créneaux occupés
            .sorted(Comparator.comparing(CreneauHoraire::getJourSemaine)
                   .thenComparing(CreneauHoraire::getHeureDebut))
            .collect(Collectors.toList());
        
        System.out.println("✅ " + creneauxReels.size() + " créneaux chargés depuis la BDD");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Rectangle pageSize = new Rectangle(PageSize.A4.rotate());
        Document document = new Document(pageSize);
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Titre
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Emploi du Temps - " + emploiId, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));
        
        // Date
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Paragraph date = new Paragraph("Généré le: " + LocalDate.now().format(DATE_FORMATTER), normalFont);
        date.setAlignment(Element.ALIGN_CENTER);
        document.add(date);
        document.add(new Paragraph(" "));
        
        if (creneauxReels.isEmpty()) {
            Paragraph empty = new Paragraph("Aucun cours trouvé pour cet emploi du temps", 
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.RED));
            empty.setAlignment(Element.ALIGN_CENTER);
            document.add(empty);
        } else {
            // Tableau
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            
            // En-têtes
            String[] headers = {"Jour", "Heure début", "Heure fin", "Classe", "Matière", "Enseignant", "Salle"};
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
            
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(new Color(44, 62, 80));
                cell.setPadding(8);
                table.addCell(cell);
            }
            
            // Données - VRAIES données de la BDD
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            for (CreneauHoraire creneau : creneauxReels) {
                table.addCell(createCell(creneau.getJourSemaine(), cellFont));
                table.addCell(createCell(creneau.getHeureDebut(), cellFont));
                table.addCell(createCell(creneau.getHeureFin(), cellFont));
                table.addCell(createCell(
                    creneau.getClasse() != null ? creneau.getClasse().getNom() : "", 
                    cellFont));
                table.addCell(createCell(
                    creneau.getMatiere() != null ? creneau.getMatiere().getNom() : "", 
                    cellFont));
                table.addCell(createCell(
                    creneau.getEnseignant() != null ? 
                        creneau.getEnseignant().getNom() + " " + creneau.getEnseignant().getPrenom() : "", 
                    cellFont));
                table.addCell(createCell(
                    creneau.getSalle() != null ? creneau.getSalle() : "", 
                    cellFont));
            }
            
            document.add(table);
        }
        
        // Pied de page
        document.add(new Paragraph(" "));
        Paragraph footer = new Paragraph("Généré automatiquement par EDT Generator", 
            FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
        
        document.close();
        return baos.toByteArray();
    }
    
    /**
     * Génère un fichier Excel avec les VRAIES données de la base
     */
    private byte[] genererExcel(String emploiId) throws Exception {
        // Récupérer les VRAIS créneaux depuis la base de données
        List<CreneauHoraire> creneauxReels = creneauHoraireRepository
            .findByEmploiDuTempsId(emploiId)
            .stream()
            .filter(c -> !c.isEstLibre())
            .sorted(Comparator.comparing(CreneauHoraire::getJourSemaine)
                   .thenComparing(CreneauHoraire::getHeureDebut))
            .collect(Collectors.toList());
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Emploi du Temps");
            
            // Style pour l'en-tête
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            
            // Créer l'en-tête
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Jour", "Heure début", "Heure fin", "Classe", "Matière", "Enseignant", "Salle"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Données - VRAIES données de la BDD
            int rowNum = 1;
            for (CreneauHoraire creneau : creneauxReels) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(creneau.getJourSemaine());
                row.createCell(1).setCellValue(creneau.getHeureDebut());
                row.createCell(2).setCellValue(creneau.getHeureFin());
                row.createCell(3).setCellValue(
                    creneau.getClasse() != null ? creneau.getClasse().getNom() : "");
                row.createCell(4).setCellValue(
                    creneau.getMatiere() != null ? creneau.getMatiere().getNom() : "");
                row.createCell(5).setCellValue(
                    creneau.getEnseignant() != null ? 
                        creneau.getEnseignant().getNom() + " " + creneau.getEnseignant().getPrenom() : "");
                row.createCell(6).setCellValue(
                    creneau.getSalle() != null ? creneau.getSalle() : "");
            }
            
            // Ajuster la largeur des colonnes
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        }
    }
    
    /**
     * Crée une cellule PDF
     */
    private PdfPCell createCell(String content, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setPadding(5);
        return cell;
    }
    
    /**
     * SUPPRIMEZ COMPLÈTEMENT cette méthode !
     * Elle était la cause du problème avec ses données fictives.
     */
    // private List<Map<String, Object>> recupererCreneauxPourExport(String emploiId) { ... }
    
    /**
     * Récupère les informations générales de l'emploi du temps
     */
    private Map<String, Object> recupererInfosEmploi(String emploiId) {
        Map<String, Object> infos = new HashMap<>();
        infos.put("id", emploiId);
        infos.put("description", "Emploi du temps généré automatiquement");
        infos.put("periode", LocalDate.now().getYear() + "-" + (LocalDate.now().getYear() + 1));
        return infos;
    }



    /**
     * EXPORT PDF POUR ENSEIGNANT - Format tableau par classe
     */
    @GetMapping("/enseignant/{enseignantId}")
    public ResponseEntity<byte[]> exporterPDFPourEnseignant(
            @PathVariable String enseignantId,
            @RequestParam String anneeScolaire) {
        try {
            System.out.println("👤 Export PDF pour enseignant: " + enseignantId);
            
            byte[] pdfContent = exportEnseignantService.exporterPDFPourEnseignant(enseignantId, anneeScolaire);
            
            // Récupérer le nom de l'enseignant pour le nom du fichier
            String enseignantNom = "enseignant";
            try {
                Enseignant enseignant = enseignantRepository.findById(enseignantId).orElse(null);
                if (enseignant != null) {
                    enseignantNom = (enseignant.getNom() + "_" + (enseignant.getPrenom() != null ? enseignant.getPrenom() : ""))
                        .replace(" ", "_");
                }
            } catch (Exception e) {
                // Ignorer
            }
            
            String filename = "EDT_" + enseignantNom + "_" + 
                            LocalDate.now().format(FILENAME_DATE_FORMATTER) + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContent);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur export PDF enseignant: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
        /**
     * EXPORT PDF POUR ENSEIGNANT - TOUTES LES CLASSES REGROUPÉES
     */
    @GetMapping("/enseignant/toutes-classes/{enseignantId}")
    public ResponseEntity<byte[]> exporterPDFEnseignantToutesClasses(
            @PathVariable String enseignantId,
            @RequestParam String anneeScolaire) {
        try {
            System.out.println("👤 Export PDF pour enseignant (toutes classes): " + enseignantId);
            
            byte[] pdfContent = exportEnseignantService.exporterPDFPourEnseignantToutesClasses(enseignantId, anneeScolaire);
            
            String enseignantNom = "enseignant";
            try {
                Enseignant enseignant = enseignantRepository.findById(enseignantId).orElse(null);
                if (enseignant != null) {
                    enseignantNom = (enseignant.getNom() + "_" + (enseignant.getPrenom() != null ? enseignant.getPrenom() : ""))
                        .replace(" ", "_");
                }
            } catch (Exception e) {}
            
            String filename = "EDT_" + enseignantNom + "_toutes_classes_" + 
                            LocalDate.now().format(FILENAME_DATE_FORMATTER) + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContent);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur export PDF enseignant toutes classes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }



    /**
     * EXPORT PDF ENSEIGNANT FORMATTÉ - Format tableau par classe
     */
    @GetMapping("/enseignant/formatte/{enseignantId}")
    public ResponseEntity<byte[]> exporterPDFEnseignantFormatte(
            @PathVariable String enseignantId,
            @RequestParam String anneeScolaire) {
        try {
            System.out.println("👤 Export PDF enseignant formaté pour: " + enseignantId);
            
            byte[] pdfContent = exportEnseignantFormatteService.exporterPDFEnseignantFormatte(enseignantId, anneeScolaire);
            
            // Récupérer le nom de l'enseignant
            String enseignantNom = "enseignant";
            try {
                Enseignant enseignant = enseignantRepository.findById(enseignantId).orElse(null);
                if (enseignant != null) {
                    enseignantNom = (enseignant.getNom() + "_" + (enseignant.getPrenom() != null ? enseignant.getPrenom() : ""))
                        .replace(" ", "_");
                }
            } catch (Exception e) {}
            
            String filename = "EDT_" + enseignantNom + "_" + 
                            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContent);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur export PDF enseignant formaté: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

}