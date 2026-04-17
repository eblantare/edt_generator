// C:\projets\java\edt-generator\backend\src\main\java\com\edt\services\ExportPDFMatricielService.java
package com.edt.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGraphics2D;
import com.edt.entities.Ecole;
import com.edt.repository.EcoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

@Service
public class ExportPDFMatricielService {

    @Autowired
    private EmploiDuTempsMatricielService matricielService;
    
    @Autowired
    private EcoleRepository ecoleRepository;

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font PAUSE_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);
    private static final Font HORAIRE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font ECOLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font MINISTERE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

    public byte[] genererPDFMatriciel(String emploiId, String classeNom) throws Exception {
        System.out.println("\n📊 EXPORT PDF - Début de génération pour emploi: " + emploiId + ", classe: " + classeNom);
        
        Map<String, Object> matrice = matricielService.construireMatricePourClasse(emploiId, classeNom);
        
        if (matrice.containsKey("error")) {
            System.err.println("❌ EXPORT PDF - Erreur: " + matrice.get("error"));
            throw new Exception("Erreur lors de la construction de la matrice");
        }
        
        // LOGS DE DIAGNOSTIC
        List<String> jours = (List<String>) matrice.get("jours");
        List<Map<String, Object>> lignes = (List<Map<String, Object>>) matrice.get("lignes");
        Map<String, String> entete = (Map<String, String>) matrice.get("entete");
        
        System.out.println("📊 EXPORT PDF - Matrice construite avec succès:");
        System.out.println("   - Jours: " + jours);
        System.out.println("   - Nombre de lignes: " + lignes.size());
        System.out.println("   - Classe: " + entete.get("classe"));
        System.out.println("   - Date génération: " + entete.get("dateGeneration"));
        
        // Compter le nombre total de cellules de cours
        int totalCours = 0;
        for (Map<String, Object> ligne : lignes) {
            Map<String, Map<String, String>> cellules = (Map<String, Map<String, String>>) ligne.get("cellules");
            for (String jour : jours) {
                Map<String, String> cellule = cellules.get(jour);
                if (cellule != null && "COURS".equals(cellule.get("type"))) {
                    totalCours++;
                }
            }
        }
        System.out.println("   - Total cours dans la matrice: " + totalCours);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Rectangle pageSize = new Rectangle(PageSize.A4.rotate());
        Document document = new Document(pageSize, 15, 15, 10, 5);
        PdfWriter.getInstance(document, baos);
        document.open();

        ajouterEnTeteComplet(document, matrice);
        
        PdfPTable table = creerTableauMatriciel(matrice);
        document.add(table);

        ajouterLegende(document);
        
        document.close();
        
        System.out.println("✅ EXPORT PDF - Document généré avec succès (" + baos.size() + " octets)");
        return baos.toByteArray();
    }

    private Image rendreLogoRond(String imagePath, float taille) throws Exception {
        // Charger l'image originale
        Image imageOriginale = Image.getInstance(imagePath);
        BufferedImage buffered = ImageIO.read(new java.net.URL(imagePath));
        
        // Créer une image ronde
        int size = (int) taille;
        BufferedImage imageRonde = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = imageRonde.createGraphics();
        
        // Appliquer le masque circulaire
        g2.setClip(new Ellipse2D.Float(0, 0, size, size));
        g2.drawImage(buffered, 0, 0, size, size, null);
        g2.dispose();
        
        // Convertir en Image iText
        Image logoRond = Image.getInstance(imageRonde, null);
        logoRond.scaleToFit(taille, taille);
        logoRond.setAlignment(Element.ALIGN_CENTER);
        
        return logoRond;
    }

    @SuppressWarnings("unchecked")
    private void ajouterEnTeteComplet(Document document, Map<String, Object> matrice) throws DocumentException {
        Map<String, String> entete = (Map<String, String>) matrice.get("entete");
        String classe = entete.get("classe");
        String anneeScolaire = entete.getOrDefault("anneeScolaire", "2025-2026");
        String dateGeneration = entete.getOrDefault("dateGeneration", 
            LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        Ecole ecole = ecoleRepository.findFirstByOrderByCreatedAtAsc().orElse(null);
        
        // === PREMIÈRE LIGNE : Trois colonnes avec espacement réduit ===
        PdfPTable ligne1 = new PdfPTable(3);
        ligne1.setWidthPercentage(100);
        ligne1.setWidths(new float[]{1.2f, 1.6f, 1.2f});
        
        // Cellule 1 - Ministère (gauche)
        PdfPCell ministereCell = new PdfPCell();
        ministereCell.setBorder(Rectangle.NO_BORDER);
        ministereCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        ministereCell.setVerticalAlignment(Element.ALIGN_TOP);
        ministereCell.setPaddingTop(0);
        ministereCell.setPaddingBottom(0);
        
        Paragraph ministere = new Paragraph();
        ministere.add(new Chunk("MINISTERE DE L'EDUCATION NATIONALE\n", MINISTERE_FONT));
        if (ecole != null) {
            ministere.add(new Chunk("D.R.E: " + (ecole.getDre() != null ? ecole.getDre() : "______________") + "\n", SUBTITLE_FONT));
            ministere.add(new Chunk("I.E.S.G: " + (ecole.getIesg() != null ? ecole.getIesg() : "______________"), SUBTITLE_FONT));
        } else {
            ministere.add(new Chunk("D.R.E: ______________\n", SUBTITLE_FONT));
            ministere.add(new Chunk("I.E.S.G: ______________", SUBTITLE_FONT));
        }
        ministereCell.addElement(ministere);
        ligne1.addCell(ministereCell);
        
        // Cellule 2 - Conteneur central (Logo + Nom école + Infos) - ESPACES RÉDUITS
        PdfPCell centreCell = new PdfPCell();
        centreCell.setBorder(Rectangle.NO_BORDER);
        centreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        centreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        centreCell.setPaddingTop(0);
        centreCell.setPaddingBottom(0);

        // Tableau interne pour empiler les éléments
        PdfPTable centreTable = new PdfPTable(1);
        centreTable.setWidthPercentage(60);
        centreTable.setHorizontalAlignment(Element.ALIGN_CENTER);

        // Ligne 2.1 - Logo (rond et réduit)
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoCell.setPaddingBottom(2);
        logoCell.setPaddingTop(0);

        if (ecole != null && ecole.getLogo() != null && !ecole.getLogo().isEmpty()) {
            try {
                Image logoRond = rendreLogoRond(ecole.getLogo(), 50);
                logoCell.addElement(logoRond);
            } catch (Exception e) {
                Paragraph logoText = new Paragraph("LOGO", NORMAL_FONT);
                logoText.setAlignment(Element.ALIGN_CENTER);
                logoCell.addElement(logoText);
            }
        } else {
            Paragraph logoText = new Paragraph("LOGO", NORMAL_FONT);
            logoText.setAlignment(Element.ALIGN_CENTER);
            logoCell.addElement(logoText);
        }
        centreTable.addCell(logoCell);

        // Ligne 2.2 - Nom de l'école (espace réduit)
        PdfPCell nomCell = new PdfPCell();
        nomCell.setBorder(Rectangle.NO_BORDER);
        nomCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        nomCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        nomCell.setPaddingTop(1);
        nomCell.setPaddingBottom(1);

        Paragraph nomEcole = new Paragraph();
        if (ecole != null && ecole.getNom() != null && !ecole.getNom().isEmpty()) {
            nomEcole.add(new Chunk(ecole.getNom(), ECOLE_FONT));
        } else {
            nomEcole.add(new Chunk("____________________", ECOLE_FONT));
        }
        nomEcole.setAlignment(Element.ALIGN_CENTER);
        nomCell.addElement(nomEcole);
        centreTable.addCell(nomCell);

        // Ligne 2.3 - Téléphone et BP (espace réduit)
        PdfPCell contactCell = new PdfPCell();
        contactCell.setBorder(Rectangle.NO_BORDER);
        contactCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        contactCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        contactCell.setPaddingTop(1);
        contactCell.setPaddingBottom(1);

        Paragraph contact = new Paragraph();
        if (ecole != null) {
            String tel = ecole.getTelephone() != null ? ecole.getTelephone() : "____________________";
            String bp = ecole.getBp() != null ? ecole.getBp() : "____________________";
            contact.add(new Chunk("Tél: " + tel + "  ", NORMAL_FONT));
            contact.add(new Chunk("BP: " + bp, NORMAL_FONT));
        } else {
            contact.add(new Chunk("Tél: ____________________  BP: ____________________", NORMAL_FONT));
        }
        contact.setAlignment(Element.ALIGN_CENTER);
        contactCell.addElement(contact);
        centreTable.addCell(contactCell);

        // Ligne 2.4 - Devise (espace réduit)
        PdfPCell deviseCell = new PdfPCell();
        deviseCell.setBorder(Rectangle.NO_BORDER);
        deviseCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        deviseCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        deviseCell.setPaddingTop(1);
        deviseCell.setPaddingBottom(0);

        Paragraph devise = new Paragraph();
        if (ecole != null && ecole.getDevise() != null && !ecole.getDevise().isEmpty()) {
            devise.add(new Chunk(ecole.getDevise(), SUBTITLE_FONT));
        } else {
            devise.add(new Chunk("Travail-Liberté-Patrie", SUBTITLE_FONT));
        }
        devise.setAlignment(Element.ALIGN_CENTER);
        deviseCell.addElement(devise);
        centreTable.addCell(deviseCell);

        centreCell.addElement(centreTable);
        ligne1.addCell(centreCell);
        
        // Cellule 3 - République (droite)
        PdfPCell republiqueCell = new PdfPCell();
        republiqueCell.setBorder(Rectangle.NO_BORDER);
        republiqueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        republiqueCell.setVerticalAlignment(Element.ALIGN_TOP);
        republiqueCell.setPaddingTop(0);
        
        Paragraph republique = new Paragraph();
        republique.add(new Chunk("REPUBLIQUE TOGOLAISE\n", MINISTERE_FONT));
        republique.add(new Chunk("Travail-Liberté-Patrie", SUBTITLE_FONT));
        republique.setAlignment(Element.ALIGN_RIGHT);
        republiqueCell.addElement(republique);
        ligne1.addCell(republiqueCell);
        
        document.add(ligne1);
        document.add(new Paragraph(" "));
        
        // === DEUXIÈME LIGNE : Titre de l'emploi du temps ===
        PdfPTable ligne2 = new PdfPTable(1);
        ligne2.setWidthPercentage(80);
        ligne2.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        PdfPCell titreCell = new PdfPCell();
        titreCell.setBorder(Rectangle.NO_BORDER);
        titreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titreCell.setPaddingTop(2);
        
        Paragraph titre = new Paragraph(
            "EMPLOI DU TEMPS - " + classe,
            TITLE_FONT
        );
        titre.setAlignment(Element.ALIGN_CENTER);
        titreCell.addElement(titre);

        Paragraph sousTitre = new Paragraph(
            "Année scolaire " + anneeScolaire + 
            " | Généré le " + dateGeneration,
            SUBTITLE_FONT
        );
        sousTitre.setAlignment(Element.ALIGN_CENTER);
        titreCell.addElement(sousTitre);
        
        ligne2.addCell(titreCell);
        
        document.add(ligne2);
        document.add(new Paragraph(" "));
    }

    @SuppressWarnings("unchecked")
    private PdfPTable creerTableauMatriciel(Map<String, Object> matrice) {
        List<String> jours = (List<String>) matrice.get("jours");
        List<Map<String, Object>> lignes = (List<Map<String, Object>>) matrice.get("lignes");

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingBefore(0);
        
        try {
            table.setWidths(new float[]{1.2f, 1f, 1f, 1f, 1f, 1f});
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        // En-tête
        PdfPCell horaireHeader = new PdfPCell(new Phrase("Horaire", HORAIRE_FONT));
        horaireHeader.setBackgroundColor(new Color(44, 62, 80));
        horaireHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        horaireHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
        horaireHeader.setPadding(5);
        table.addCell(horaireHeader);

        for (String jour : jours) {
            PdfPCell cell = new PdfPCell(new Phrase(jour, HEADER_FONT));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(new Color(44, 62, 80));
            cell.setPadding(5);
            table.addCell(cell);
        }

        // Lignes de cours
        for (Map<String, Object> ligne : lignes) {
            String horaire = (String) ligne.get("horaire");
            String type = (String) ligne.get("type");
            Map<String, Map<String, String>> cellules = 
                (Map<String, Map<String, String>>) ligne.get("cellules");

            PdfPCell horaireCell = new PdfPCell(new Phrase(horaire, HORAIRE_FONT));
            horaireCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            horaireCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            horaireCell.setBackgroundColor(new Color(236, 240, 241));
            horaireCell.setPadding(5);
            table.addCell(horaireCell);

            for (String jour : jours) {
                Map<String, String> cellule = cellules.get(jour);
                PdfPCell cell = creerCellule(cellule, type);
                table.addCell(cell);
            }
        }

        return table;
    }

    private PdfPCell creerCellule(Map<String, String> cellule, String typeLigne) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(5);
        cell.setMinimumHeight(22f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        if (cellule == null) {
            cell.setPhrase(new Phrase("-", NORMAL_FONT));
            cell.setGrayFill(0.98f);
            return cell;
        }

        String typeCellule = cellule.getOrDefault("type", "VIDE");

        if ("PAUSE".equals(typeCellule) || "RECREATION".equals(typeLigne) || "GRANDE PAUSE".equals(typeLigne)) {
            String libelle = cellule.getOrDefault("libelle", typeLigne);
            cell.setPhrase(new Phrase(libelle, PAUSE_FONT));
            cell.setBackgroundColor(new Color(255, 243, 205));
        } else if ("COURS".equals(typeCellule)) {
            String matiere = cellule.getOrDefault("matiere", "");
            String enseignant = cellule.getOrDefault("enseignant", "");
        
            String enseignantAbrege = "";
            if (enseignant != null && !enseignant.isEmpty()) {
                String nomPropre = enseignant.replace("M. ", "").replace("M.", "").trim();
                enseignantAbrege = nomPropre.length() >= 3 ? 
                    nomPropre.substring(0, 3).toUpperCase() : nomPropre.toUpperCase();
            }
        
            Paragraph p = new Paragraph();
            p.add(new Chunk(matiere + "\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8)));
            p.add(new Chunk(enseignantAbrege, FontFactory.getFont(FontFactory.HELVETICA, 7, Color.DARK_GRAY)));
            p.setAlignment(Element.ALIGN_CENTER);
        
            cell.setPhrase(p);
            cell.setBackgroundColor(new Color(232, 244, 253));
        } else {
            cell.setPhrase(new Phrase("-", NORMAL_FONT));
            cell.setGrayFill(0.98f);
        }

        return cell;
    }

    private void ajouterLegende(Document document) throws DocumentException {
        Paragraph legend = new Paragraph();
        legend.add(new Chunk("MATIN: 07h00-12h00 | ", NORMAL_FONT));
        legend.add(new Chunk("SOIR: 15h00-17h45 (LUNDI,MARDI,JEUDI) | ", NORMAL_FONT));
        legend.add(new Chunk("RÉCRÉATION: 09h45-10h10 | ", NORMAL_FONT));
        legend.add(new Chunk("PAUSE MÉRIDIENNE: 12h00-15h00", NORMAL_FONT));
        
        legend.setAlignment(Element.ALIGN_CENTER);
        legend.setSpacingBefore(2);
        document.add(legend);
        
        Paragraph footer = new Paragraph(
            "Généré automatiquement par EDT Generator",
            FontFactory.getFont(FontFactory.HELVETICA, 6, Color.GRAY)
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(2);
        document.add(footer);
    }
}