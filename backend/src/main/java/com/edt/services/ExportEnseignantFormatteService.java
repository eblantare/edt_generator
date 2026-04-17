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

@Service
public class ExportEnseignantFormatteService {
    
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
    
    public byte[] exporterPDFEnseignantFormatte(String enseignantId, String anneeScolaire) throws Exception {
        
        // ========== DIAGNOSTIC ==========
        System.out.println("\n" + "🔍".repeat(30));
        System.out.println("🔍 EXPORT ENSEIGNANT FORMATTÉ - DIAGNOSTIC");
        System.out.println("🔍".repeat(30));
        System.out.println("👤 Enseignant ID: " + enseignantId);
        System.out.println("📅 Année scolaire: " + anneeScolaire);
        
        // Vérifier si l'enseignant existe
        Enseignant enseignant = enseignantRepository.findById(enseignantId)
            .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));
        
        System.out.println("✅ Enseignant trouvé: " + enseignant.getNom() + " " + 
            (enseignant.getPrenom() != null ? enseignant.getPrenom() : ""));
        
        // Récupérer TOUS les créneaux (même libres) pour diagnostic
        List<CreneauHoraire> tousCreneauxBrut = creneauHoraireRepository
            .findByEnseignantIdAndEmploiDuTemps_AnneeScolaire(enseignantId, anneeScolaire);
        
        System.out.println("📊 Créneaux trouvés (total): " + tousCreneauxBrut.size());
        
        // Compter les libres et non libres
        long libres = tousCreneauxBrut.stream().filter(c -> c.isEstLibre()).count();
        long nonLibres = tousCreneauxBrut.stream().filter(c -> !c.isEstLibre()).count();
        
        System.out.println("   - Créneaux libres: " + libres);
        System.out.println("   - Créneaux occupés (cours): " + nonLibres);
        
        // Récupérer seulement les cours (non libres)
        List<CreneauHoraire> tousCreneaux = tousCreneauxBrut.stream()
            .filter(c -> !c.isEstLibre())
            .collect(Collectors.toList());
        
        System.out.println("📚 Total cours trouvés: " + tousCreneaux.size() + " heures");
        
        if (tousCreneaux.isEmpty()) {
            System.err.println("❌ AUCUN COURS TROUVÉ pour cet enseignant !");
            throw new RuntimeException("Aucun cours trouvé pour cet enseignant en " + anneeScolaire);
        }
        
        // Afficher les premiers cours pour vérification
        System.out.println("\n📋 Détail des premiers cours trouvés:");
        for (int i = 0; i < Math.min(5, tousCreneaux.size()); i++) {
            CreneauHoraire c = tousCreneaux.get(i);
            String horaireDB = c.getHeureDebut() + "-" + c.getHeureFin();
            System.out.println("   " + (i+1) + ". " + 
                c.getJourSemaine() + " " + horaireDB + " | " +
                (c.getMatiere() != null ? c.getMatiere().getCode() : "?") + " | " +
                (c.getClasse() != null ? c.getClasse().getNom() : "?") + " | " +
                (c.getSalle() != null ? c.getSalle() : "?"));
        }
        
        // ========== CONSTRUCTION DU PDF ==========
        System.out.println("\n📄 Construction du PDF...");
        
        // Indexer les créneaux par (jour, horaire) - avec conversion du format
        Map<String, Map<String, CreneauHoraire>> indexCreneaux = new HashMap<>();
        for (CreneauHoraire c : tousCreneaux) {
            if (!indexCreneaux.containsKey(c.getJourSemaine())) {
                indexCreneaux.put(c.getJourSemaine(), new HashMap<>());
            }
            
            // Convertir le format de la base (HH:MM-HH:MM) au format d'affichage (HHhMM-HHhMM)
            String horaireDB = c.getHeureDebut() + "-" + c.getHeureFin();
            String horaireAffichage = horaireDB.replace(":", "h");
            
            indexCreneaux.get(c.getJourSemaine()).put(horaireAffichage, c);
            
            // Debug : afficher la conversion
            System.out.println("   🔄 Conversion: " + horaireDB + " -> " + horaireAffichage);
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
        PdfWriter.getInstance(document, baos);
        document.open();
        
        // Titre
        Paragraph titre = new Paragraph(
            "EMPLOI DU TEMPS - " + enseignant.getNom().toUpperCase() + 
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
        
        // Créer le tableau avec les jours en colonnes
        PdfPTable table = new PdfPTable(6); // 1 colonne heure + 5 jours
        table.setWidthPercentage(100);
        try {
            table.setWidths(new float[]{1.2f, 1f, 1f, 1f, 1f, 1f});
        } catch (Exception e) {
            // Ignorer
        }
        
        // En-têtes
        PdfPCell emptyHeader = new PdfPCell(new Phrase("", HEADER_FONT));
        emptyHeader.setBackgroundColor(new Color(44, 62, 80));
        emptyHeader.setPadding(5);
        table.addCell(emptyHeader);
        
        for (String jour : JOURS) {
            PdfPCell cell = new PdfPCell(new Phrase(jour, HEADER_FONT));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(new Color(44, 62, 80));
            cell.setPadding(5);
            table.addCell(cell);
        }
        
        // Remplir le tableau ligne par ligne
        int coursAjoutes = 0;
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
            
            // Pour chaque jour
            for (String jour : JOURS) {
                // Ignorer les créneaux du soir pour les jours sans soir
                if ((ordre == 8 || ordre == 9 || ordre == 10) && !JOURS_SOIR.contains(jour)) {
                    PdfPCell emptyCell = new PdfPCell(new Phrase("", NORMAL_FONT));
                    emptyCell.setGrayFill(0.98f);
                    emptyCell.setPadding(5);
                    table.addCell(emptyCell);
                    continue;
                }
                
                // Pauses
                if (type.equals("RECREATION") || type.equals("GRANDE PAUSE")) {
                    PdfPCell pauseCell = new PdfPCell(new Phrase(type, PAUSE_FONT));
                    pauseCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    pauseCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    pauseCell.setBackgroundColor(new Color(255, 243, 205));
                    pauseCell.setPadding(5);
                    table.addCell(pauseCell);
                    continue;
                }
                
                // Chercher le cours pour ce jour et cette heure
                CreneauHoraire creneau = null;
                if (indexCreneaux.containsKey(jour)) {
                    creneau = indexCreneaux.get(jour).get(horaire);
                }
                
                PdfPCell cell;
                if (creneau != null) {
                    coursAjoutes++;
                    // Format: "FR - 6 ème" ou "ECM - 4 ème"
                    String matiereCode = creneau.getMatiere().getCode();
                    // Prendre seulement les 2-3 premières lettres pour FR, ECM, etc.
                    if (matiereCode.length() > 3) {
                        matiereCode = matiereCode.substring(0, 3);
                    }
                    String contenu = matiereCode + " - " + creneau.getClasse().getNom();
                    cell = new PdfPCell(new Phrase(contenu, NORMAL_FONT));
                    cell.setBackgroundColor(new Color(232, 244, 253));
                } else {
                    cell = new PdfPCell(new Phrase("", NORMAL_FONT));
                    cell.setGrayFill(0.98f);
                }
                
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(5);
                table.addCell(cell);
            }
        }
        
        document.add(table);
        
        // Légende
        Paragraph legend = new Paragraph();
        legend.add(new Chunk("MATIN: 07h00-12h00 | ", NORMAL_FONT));
        legend.add(new Chunk("SOIR: 15h00-17h45 (LUNDI,MARDI,JEUDI) | ", NORMAL_FONT));
        legend.add(new Chunk("RÉCRÉATION: 09h45-10h10 | ", NORMAL_FONT));
        legend.add(new Chunk("PAUSE MÉRIDIENNE: 12h00-15h00", NORMAL_FONT));
        legend.setAlignment(Element.ALIGN_CENTER);
        legend.setSpacingBefore(5);
        document.add(legend);
        
        document.close();
        
        System.out.println("\n✅ PDF généré avec " + coursAjoutes + " cours affichés sur " + tousCreneaux.size() + " trouvés");
        System.out.println("🔍".repeat(30) + "\n");
        
        return baos.toByteArray();
    }
}