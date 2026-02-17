// C:\projets\java\edt-generator\backend\src\main\java\com\edt\services\ExportPDFMatricielService.java
package com.edt.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class ExportPDFMatricielService {

    @Autowired
    private EmploiDuTempsMatricielService matricielService;

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font PAUSE_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);
    private static final Font HORAIRE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

    public byte[] genererPDFMatriciel(String emploiId, String classeNom) throws Exception {
        // 1. Construire la matrice depuis la BDD
        Map<String, Object> matrice = matricielService.construireMatricePourClasse(emploiId, classeNom);
        
        // Vérifier si la matrice contient une erreur
        if (matrice.containsKey("error")) {
            throw new Exception("Erreur lors de la construction de la matrice");
        }
        
        // 2. Créer le document PDF en paysage
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Rectangle pageSize = new Rectangle(PageSize.A4.rotate());
        Document document = new Document(pageSize);
        PdfWriter.getInstance(document, baos);
        document.open();

        // 3. En-tête du document
        ajouterEnTete(document, matrice);

        // 4. Tableau principal
        PdfPTable table = creerTableauMatriciel(matrice);
        document.add(table);

        // 5. Légende et pied de page
        ajouterLegende(document);
        
        document.close();
        return baos.toByteArray();
    }

    @SuppressWarnings("unchecked")
    private void ajouterEnTete(Document document, Map<String, Object> matrice) throws DocumentException {
        Map<String, String> entete = (Map<String, String>) matrice.get("entete");
        
        Paragraph titre = new Paragraph(
            entete.get("classe") + " - EMPLOI DU TEMPS",
            TITLE_FONT
        );
        titre.setAlignment(Element.ALIGN_CENTER);
        document.add(titre);

        String anneeScolaire = entete.getOrDefault("anneeScolaire", "2025-2026");
        String dateGeneration = entete.getOrDefault("dateGeneration", "");
        
        Paragraph sousTitre = new Paragraph(
            "Année scolaire " + anneeScolaire + 
            " | Généré le " + dateGeneration,
            FontFactory.getFont(FontFactory.HELVETICA, 10)
        );
        sousTitre.setAlignment(Element.ALIGN_CENTER);
        document.add(sousTitre);
        
        document.add(new Paragraph(" "));
    }

    @SuppressWarnings("unchecked")
    private PdfPTable creerTableauMatriciel(Map<String, Object> matrice) {
        List<String> jours = (List<String>) matrice.get("jours");
        List<Map<String, Object>> lignes = (List<Map<String, Object>>) matrice.get("lignes");

        // Tableau : 1 colonne horaire + 5 colonnes jours
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        
        try {
            table.setWidths(new float[]{1.5f, 1f, 1f, 1f, 1f, 1f});
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        // === LIGNE 1 : En-tête des jours ===
        // Cellule vide pour la colonne horaire
        PdfPCell emptyCell = new PdfPCell(new Phrase(""));
        emptyCell.setBackgroundColor(new Color(44, 62, 80));
        emptyCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(emptyCell);

        // Cellules pour chaque jour
        for (String jour : jours) {
            PdfPCell cell = new PdfPCell(new Phrase(jour, HEADER_FONT));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(new Color(44, 62, 80));
            cell.setPadding(8);
            table.addCell(cell);
        }

        // === LIGNES SUIVANTES : Créneaux ===
        for (Map<String, Object> ligne : lignes) {
            String horaire = (String) ligne.get("horaire");
            String type = (String) ligne.get("type");
            Map<String, Map<String, String>> cellules = 
                (Map<String, Map<String, String>>) ligne.get("cellules");

            // Cellule horaire
            PdfPCell horaireCell = new PdfPCell(new Phrase(horaire, HORAIRE_FONT));
            horaireCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            horaireCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            horaireCell.setBackgroundColor(new Color(236, 240, 241));
            horaireCell.setPadding(5);
            table.addCell(horaireCell);

            // Cellules pour chaque jour
            for (String jour : jours) {
                Map<String, String> cellule = cellules.get(jour);
                PdfPCell cell = creerCellule(cellule, type);
                table.addCell(cell);
            }
        }

        return table;
    }

    // C:\projets\java\edt-generator\backend\src\main\java\com\edt\services\ExportPDFMatricielService.java
   // MODIFIEZ UNIQUEMENT LA MÉTHODE creerCellule() COMME CECI :

    private PdfPCell creerCellule(Map<String, String> cellule, String typeLigne) {
       PdfPCell cell = new PdfPCell();
       cell.setPadding(5);
       cell.setMinimumHeight(25f); // Augmente la hauteur minimum
       cell.setHorizontalAlignment(Element.ALIGN_CENTER);
       cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

       if (cellule == null) {
           cell.setPhrase(new Phrase("-", NORMAL_FONT));
           cell.setGrayFill(0.98f);
           return cell;
       }

        String typeCellule = cellule.getOrDefault("type", "VIDE");

        if ("PAUSE".equals(typeCellule) || "RECREATION".equals(typeLigne) || "GRANDE PAUSE".equals(typeLigne)) {
           // Cellule de pause
           String libelle = cellule.getOrDefault("libelle", typeLigne);
           Phrase phrase = new Phrase(libelle, PAUSE_FONT);
           cell.setPhrase(phrase);
           cell.setBackgroundColor(new Color(255, 243, 205));
        } else if ("COURS".equals(typeCellule)) {
           // Cellule de cours - Format sur DEUX LIGNES
           String matiere = cellule.getOrDefault("matiere", "");
           String enseignant = cellule.getOrDefault("enseignant", "");
        
           // Nettoyer l'enseignant : enlever "M. " et garder les 3 premières lettres
           String enseignantAbrege = "";
           if (enseignant != null && !enseignant.isEmpty()) {
              // Enlever "M. " si présent
               String nomPropre = enseignant.replace("M. ", "").replace("M.", "").trim();
               // Prendre les 3 premières lettres en majuscules
               if (nomPropre.length() >= 3) {
                   enseignantAbrege = nomPropre.substring(0, 3).toUpperCase();
               } else {
                  enseignantAbrege = nomPropre.toUpperCase();
               }
            }
        
            // Créer un paragraphe avec deux lignes
            Paragraph p = new Paragraph();
            p.add(new Chunk(matiere + "\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
            p.add(new Chunk(enseignantAbrege, FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY)));
        
            cell.setPhrase(p);
            cell.setBackgroundColor(new Color(232, 244, 253));
        } else {
          // Cellule vide
           cell.setPhrase(new Phrase("-", NORMAL_FONT));
           cell.setGrayFill(0.98f);
        }

        return cell;
    }

    private void ajouterLegende(Document document) throws DocumentException {
        document.add(new Paragraph(" "));
        
        Paragraph legend = new Paragraph();
        legend.add(new Chunk("MATIN: 07h00-12h00  |  ", NORMAL_FONT));
        legend.add(new Chunk("SOIR: 15h00-17h45 (LUNDI, MARDI, JEUDI)  |  ", NORMAL_FONT));
        legend.add(new Chunk("RÉCRÉATION: 09h45-10h10  |  ", NORMAL_FONT));
        legend.add(new Chunk("PAUSE MÉRIDIENNE: 12h00-15h00", NORMAL_FONT));
        
        legend.setAlignment(Element.ALIGN_CENTER);
        document.add(legend);
        
        Paragraph footer = new Paragraph(
            "Généré automatiquement par EDT Generator",
            FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY)
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }
}