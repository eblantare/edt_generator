package com.edt.services;

import com.edt.entities.*;
import com.edt.repository.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections; 

@Service
public class ExportEnseignantService {
    
    @Autowired
    private CreneauHoraireRepository creneauHoraireRepository;
    
    @Autowired
    private EnseignantRepository enseignantRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<String> JOURS = Arrays.asList("LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI");
    private static final List<String> HORAIRES = Arrays.asList(
        "07h00-07h55", "07h55-08h50", "08h50-09h45", "09h45-10h10",
        "10h10-11h05", "11h05-12h00", "12h00-15h00",
        "15h00-15h55", "15h55-16h50", "16h50-17h45"
    );
    private static final List<String> TYPES = Arrays.asList(
        "COURS", "COURS", "COURS", "RECREATION",
        "COURS", "COURS", "GRANDE PAUSE",
        "COURS", "COURS", "COURS"
    );
    private static final List<String> JOURS_SOIR = Arrays.asList("LUNDI", "MARDI", "JEUDI");
    
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font PAUSE_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);
    
    public byte[] exporterPDFPourEnseignant(String enseignantId, String anneeScolaire) throws Exception {
        Enseignant enseignant = enseignantRepository.findById(enseignantId)
            .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));
        
        System.out.println("📊 Export PDF pour enseignant: " + enseignant.getNom());
        System.out.println("   ID: " + enseignantId);
        System.out.println("   Année: " + anneeScolaire);
        
        // ✅ Utilisation de la méthode corrigée
        List<CreneauHoraire> tousCreneaux = creneauHoraireRepository
            .findByEnseignantIdAndEmploiDuTemps_AnneeScolaire(enseignantId, anneeScolaire);
        
        System.out.println("   📅 Créneaux trouvés: " + tousCreneaux.size());
        
        // Filtrer pour ne garder que les cours (non libres)
        tousCreneaux = tousCreneaux.stream()
            .filter(c -> !c.isEstLibre())
            .collect(Collectors.toList());
        
        System.out.println("   📚 Cours trouvés: " + tousCreneaux.size());
        
        if (tousCreneaux.isEmpty()) {
            throw new RuntimeException("Aucun cours trouvé pour cet enseignant en " + anneeScolaire);
        }
        
        // Grouper par classe
        Map<String, List<CreneauHoraire>> creneauxParClasse = tousCreneaux.stream()
            .filter(c -> c.getClasse() != null)
            .collect(Collectors.groupingBy(c -> c.getClasse().getNom()));
        
        System.out.println("   🏫 Classes concernées: " + creneauxParClasse.size());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
        PdfWriter.getInstance(document, baos);
        document.open();
        
        // Titre
        Paragraph titre = new Paragraph(
            "EMPLOI DU TEMPS DE " + enseignant.getNom().toUpperCase() + 
            (enseignant.getPrenom() != null ? " " + enseignant.getPrenom().toUpperCase() : ""),
            TITLE_FONT
        );
        titre.setAlignment(Element.ALIGN_CENTER);
        document.add(titre);
        
        Paragraph sousTitre = new Paragraph(
            "Année scolaire " + anneeScolaire + " | Généré le " + LocalDate.now().format(DATE_FORMATTER),
            SUBTITLE_FONT
        );
        sousTitre.setAlignment(Element.ALIGN_CENTER);
        document.add(sousTitre);
        document.add(new Paragraph(" "));
        
        // Pour chaque classe, créer un tableau
        for (Map.Entry<String, List<CreneauHoraire>> entry : creneauxParClasse.entrySet()) {
            String classeNom = entry.getKey();
            List<CreneauHoraire> creneauxClasse = entry.getValue();
            
            System.out.println("   📋 Traitement de la classe: " + classeNom + " (" + creneauxClasse.size() + " cours)");
            
            // Titre de la classe
            Paragraph classeTitre = new Paragraph("Classe: " + classeNom, 
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            classeTitre.setSpacingBefore(10);
            classeTitre.setSpacingAfter(5);
            document.add(classeTitre);
            
            // Créer le tableau
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            try {
                table.setWidths(new float[]{1.2f, 1f, 1f, 1f, 1f, 1f});
            } catch (Exception e) {
                // Ignorer
            }
            
            // En-têtes
            PdfPCell horaireHeader = new PdfPCell(new Phrase("Horaire", HEADER_FONT));
            horaireHeader.setBackgroundColor(new Color(44, 62, 80));
            horaireHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            horaireHeader.setPadding(5);
            table.addCell(horaireHeader);
            
            for (String jour : JOURS) {
                PdfPCell cell = new PdfPCell(new Phrase(jour, HEADER_FONT));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(new Color(44, 62, 80));
                cell.setPadding(5);
                table.addCell(cell);
            }
            
            // Indexer les créneaux par jour et numéro
            Map<String, CreneauHoraire> creneauxMap = new HashMap<>();
            for (CreneauHoraire c : creneauxClasse) {
                String key = c.getJourSemaine() + "_" + c.getNumeroCreneau();
                creneauxMap.put(key, c);
            }
            
            // Remplir le tableau
            for (int i = 0; i < HORAIRES.size(); i++) {
                String horaire = HORAIRES.get(i);
                String type = TYPES.get(i);
                int ordre = i + 1;
                
                // Cellule horaire
                PdfPCell horaireCell = new PdfPCell(new Phrase(horaire, NORMAL_FONT));
                horaireCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                horaireCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                horaireCell.setBackgroundColor(new Color(240, 240, 240));
                horaireCell.setPadding(5);
                table.addCell(horaireCell);
                
                // Cellules pour chaque jour
                for (String jour : JOURS) {
                    if ((ordre == 8 || ordre == 9 || ordre == 10) && !JOURS_SOIR.contains(jour)) {
                        PdfPCell emptyCell = new PdfPCell(new Phrase(""));
                        emptyCell.setGrayFill(0.98f);
                        table.addCell(emptyCell);
                        continue;
                    }
                    
                    if (type.equals("RECREATION") || type.equals("GRANDE PAUSE")) {
                        PdfPCell pauseCell = new PdfPCell(new Phrase(type, PAUSE_FONT));
                        pauseCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pauseCell.setBackgroundColor(new Color(255, 243, 205));
                        table.addCell(pauseCell);
                        continue;
                    }
                    
                    String key = jour + "_" + ordre;
                    CreneauHoraire creneau = creneauxMap.get(key);
                    
                    PdfPCell cell;
                    if (creneau != null && !creneau.isEstLibre()) {
                        Paragraph p = new Paragraph();
                        p.add(new Chunk(creneau.getMatiere().getCode() + "\n", 
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8)));
                        p.add(new Chunk(creneau.getSalle() != null ? creneau.getSalle() : "",
                            FontFactory.getFont(FontFactory.HELVETICA, 7, Color.DARK_GRAY)));
                        p.setAlignment(Element.ALIGN_CENTER);
                        
                        cell = new PdfPCell(p);
                        cell.setBackgroundColor(new Color(232, 244, 253));
                    } else {
                        cell = new PdfPCell(new Phrase("-", NORMAL_FONT));
                        cell.setGrayFill(0.98f);
                    }
                    
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setPadding(5);
                    table.addCell(cell);
                }
            }
            
            document.add(table);
            document.add(new Paragraph(" "));
        }
        
        // Légende
        Paragraph legend = new Paragraph();
        legend.add(new Chunk("MATIN: 07h00-12h00 | ", NORMAL_FONT));
        legend.add(new Chunk("SOIR: 15h00-17h45 (LUNDI,MARDI,JEUDI) | ", NORMAL_FONT));
        legend.add(new Chunk("RÉCRÉATION: 09h45-10h10 | ", NORMAL_FONT));
        legend.add(new Chunk("PAUSE MÉRIDIENNE: 12h00-15h00", NORMAL_FONT));
        legend.setAlignment(Element.ALIGN_CENTER);
        legend.setSpacingBefore(5);
        document.add(legend);
        
        Paragraph footer = new Paragraph(
            "Généré automatiquement par EDT Generator",
            FontFactory.getFont(FontFactory.HELVETICA, 6, Color.GRAY)
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(5);
        document.add(footer);
        
        document.close();
        
        System.out.println("✅ PDF généré avec succès (" + baos.size() + " octets)");
        return baos.toByteArray();
    }

    public byte[] exporterPDFPourEnseignantToutesClasses(String enseignantId, String anneeScolaire) throws Exception {
        Enseignant enseignant = enseignantRepository.findById(enseignantId)
            .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));
        
        System.out.println("📊 Export PDF pour enseignant TOUTES CLASSES: " + enseignant.getNom());
        
        // Récupérer TOUS les créneaux de cet enseignant (dans toutes les classes)
        List<CreneauHoraire> tousCreneaux = creneauHoraireRepository
            .findByEnseignantIdAndEmploiDuTemps_AnneeScolaire(enseignantId, anneeScolaire);
        
        // Filtrer pour ne garder que les cours (non libres)
        tousCreneaux = tousCreneaux.stream()
            .filter(c -> !c.isEstLibre())
            .collect(Collectors.toList());
        
        if (tousCreneaux.isEmpty()) {
            throw new RuntimeException("Aucun cours trouvé pour cet enseignant en " + anneeScolaire);
        }
        
        // Grouper par classe
        Map<String, List<CreneauHoraire>> creneauxParClasse = tousCreneaux.stream()
            .filter(c -> c.getClasse() != null)
            .collect(Collectors.groupingBy(c -> c.getClasse().getNom()));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
        PdfWriter.getInstance(document, baos);
        document.open();
        
        // Titre
        Paragraph titre = new Paragraph(
            "EMPLOI DU TEMPS DE " + enseignant.getNom().toUpperCase() + 
            (enseignant.getPrenom() != null ? " " + enseignant.getPrenom().toUpperCase() : ""),
            TITLE_FONT
        );
        titre.setAlignment(Element.ALIGN_CENTER);
        document.add(titre);
        
        Paragraph sousTitre = new Paragraph(
            "Année scolaire " + anneeScolaire + " | Toutes classes confondues",
            SUBTITLE_FONT
        );
        sousTitre.setAlignment(Element.ALIGN_CENTER);
        document.add(sousTitre);
        document.add(new Paragraph(" "));
        
        // Créer un tableau unique avec TOUTES les classes
        PdfPTable table = new PdfPTable(creneauxParClasse.size() + 1);
        table.setWidthPercentage(100);
        
        // En-têtes
        PdfPCell horaireHeader = new PdfPCell(new Phrase("Horaire", HEADER_FONT));
        horaireHeader.setBackgroundColor(new Color(44, 62, 80));
        horaireHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        horaireHeader.setPadding(5);
        table.addCell(horaireHeader);
        
        // Ajouter les classes comme en-têtes
        List<String> classesList = new ArrayList<>(creneauxParClasse.keySet());
        for (String classeNom : classesList) {
            PdfPCell cell = new PdfPCell(new Phrase(classeNom, HEADER_FONT));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(new Color(44, 62, 80));
            cell.setPadding(5);
            table.addCell(cell);
        }
        
        // Organiser les créneaux par horaire
        Map<String, Map<String, String>> coursParHoraire = new HashMap<>();
        for (Map.Entry<String, List<CreneauHoraire>> entry : creneauxParClasse.entrySet()) {
            String classeNom = entry.getKey();
            for (CreneauHoraire c : entry.getValue()) {
                String key = c.getJourSemaine() + "_" + c.getHeureDebut() + "-" + c.getHeureFin();
                if (!coursParHoraire.containsKey(key)) {
                    coursParHoraire.put(key, new HashMap<>());
                }
                coursParHoraire.get(key).put(classeNom, 
                    c.getMatiere().getCode() + "\n" + (c.getSalle() != null ? c.getSalle() : ""));
            }
        }
        
        // Trier les horaires
        List<String> horairesTries = new ArrayList<>(coursParHoraire.keySet());
        Collections.sort(horairesTries);
        
        // Remplir le tableau
        for (String horaire : horairesTries) {
            PdfPCell horaireCell = new PdfPCell(new Phrase(horaire, NORMAL_FONT));
            horaireCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            horaireCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            horaireCell.setBackgroundColor(new Color(240, 240, 240));
            horaireCell.setPadding(5);
            table.addCell(horaireCell);
            
            Map<String, String> cours = coursParHoraire.get(horaire);
            for (String classeNom : classesList) {
                String contenu = cours.getOrDefault(classeNom, "-");
                PdfPCell cell = new PdfPCell(new Phrase(contenu, NORMAL_FONT));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(5);
                if (cours.containsKey(classeNom)) {
                    cell.setBackgroundColor(new Color(232, 244, 253));
                } else {
                    cell.setGrayFill(0.98f);
                }
                table.addCell(cell);
            }
        }
        
        document.add(table);
        
        // Légende
        Paragraph legend = new Paragraph();
        legend.add(new Chunk("Généré le " + LocalDate.now().format(DATE_FORMATTER), NORMAL_FONT));
        legend.setAlignment(Element.ALIGN_CENTER);
        legend.setSpacingBefore(5);
        document.add(legend);
        
        document.close();
        
        return baos.toByteArray();
    }
}