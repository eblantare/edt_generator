package com.edt.dtos;

import java.time.LocalDate;
import java.util.List;

public class EmploiDuTempsDTO {
    private String id;
    private String nom;
    private String anneeScolaire;
    private LocalDate dateGeneration;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String statut; // "BROUILLON", "VALIDE", "PUBLIE", "ARCHIVE"
    private String classeId;
    private String classeNom;
    private String filiere;
    private String niveauClasse;
    private Boolean estGlobal;
    private Integer nombreCreneaux;
    private Integer nombreCreneauxOccupes;
    private Integer nombreCreneauxLibres;
    private String createurId;
    private String createurNom;
    private LocalDate dateValidation;
    private String validateurId;
    private String validateurNom;
    private List<String> joursCours; // ["LUNDI", "MARDI", "JEUDI"]
    private String commentaire;

    // Constructeurs
    public EmploiDuTempsDTO() {}

    public EmploiDuTempsDTO(String id, String nom, String anneeScolaire, 
                           String classeId, Boolean estGlobal) {
        this.id = id;
        this.nom = nom;
        this.anneeScolaire = anneeScolaire;
        this.classeId = classeId;
        this.estGlobal = estGlobal;
        this.dateGeneration = LocalDate.now();
        this.statut = "BROUILLON";
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getAnneeScolaire() { return anneeScolaire; }
    public void setAnneeScolaire(String anneeScolaire) { this.anneeScolaire = anneeScolaire; }

    public LocalDate getDateGeneration() { return dateGeneration; }
    public void setDateGeneration(LocalDate dateGeneration) { this.dateGeneration = dateGeneration; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getClasseId() { return classeId; }
    public void setClasseId(String classeId) { this.classeId = classeId; }

    public String getClasseNom() { return classeNom; }
    public void setClasseNom(String classeNom) { this.classeNom = classeNom; }

    public String getFiliere() { return filiere; }
    public void setFiliere(String filiere) { this.filiere = filiere; }

    public String getNiveauClasse() { return niveauClasse; }
    public void setNiveauClasse(String niveauClasse) { this.niveauClasse = niveauClasse; }

    public Boolean getEstGlobal() { return estGlobal; }
    public void setEstGlobal(Boolean estGlobal) { this.estGlobal = estGlobal; }

    public Integer getNombreCreneaux() { return nombreCreneaux; }
    public void setNombreCreneaux(Integer nombreCreneaux) { this.nombreCreneaux = nombreCreneaux; }

    public Integer getNombreCreneauxOccupes() { return nombreCreneauxOccupes; }
    public void setNombreCreneauxOccupes(Integer nombreCreneauxOccupes) { this.nombreCreneauxOccupes = nombreCreneauxOccupes; }

    public Integer getNombreCreneauxLibres() { return nombreCreneauxLibres; }
    public void setNombreCreneauxLibres(Integer nombreCreneauxLibres) { this.nombreCreneauxLibres = nombreCreneauxLibres; }

    public String getCreateurId() { return createurId; }
    public void setCreateurId(String createurId) { this.createurId = createurId; }

    public String getCreateurNom() { return createurNom; }
    public void setCreateurNom(String createurNom) { this.createurNom = createurNom; }

    public LocalDate getDateValidation() { return dateValidation; }
    public void setDateValidation(LocalDate dateValidation) { this.dateValidation = dateValidation; }

    public String getValidateurId() { return validateurId; }
    public void setValidateurId(String validateurId) { this.validateurId = validateurId; }

    public String getValidateurNom() { return validateurNom; }
    public void setValidateurNom(String validateurNom) { this.validateurNom = validateurNom; }

    public List<String> getJoursCours() { return joursCours; }
    public void setJoursCours(List<String> joursCours) { this.joursCours = joursCours; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    // Méthodes utilitaires
    public String getTypeEmploi() {
        return estGlobal ? "Global (tous les enseignants)" : "Par classe";
    }

    public String getDescriptionComplete() {
        if (estGlobal) {
            return nom + " - " + anneeScolaire + " (Global)";
        } else {
            return nom + " - " + classeNom + " - " + anneeScolaire;
        }
    }

    public Double getTauxOccupation() {
        if (nombreCreneaux == null || nombreCreneaux == 0) {
            return 0.0;
        }
        if (nombreCreneauxOccupes == null) {
            return 0.0;
        }
        return (nombreCreneauxOccupes * 100.0) / nombreCreneaux;
    }

    public String getStatutCouleur() {
        switch (statut) {
            case "BROUILLON": return "warning";
            case "VALIDE": return "success";
            case "PUBLIE": return "info";
            case "ARCHIVE": return "secondary";
            default: return "light";
        }
    }

    public String getStatutIcone() {
        switch (statut) {
            case "BROUILLON": return "bi-pencil";
            case "VALIDE": return "bi-check-circle";
            case "PUBLIE": return "bi-eye";
            case "ARCHIVE": return "bi-archive";
            default: return "bi-file";
        }
    }

    public Boolean estModifiable() {
        return "BROUILLON".equals(statut) || "VALIDE".equals(statut);
    }

    public Boolean estPublic() {
        return "PUBLIE".equals(statut);
    }

    @Override
    public String toString() {
        return "EmploiDuTempsDTO{" +
                "nom='" + nom + '\'' +
                ", anneeScolaire='" + anneeScolaire + '\'' +
                ", classeNom='" + classeNom + '\'' +
                ", estGlobal=" + estGlobal +
                ", statut='" + statut + '\'' +
                ", dateGeneration=" + dateGeneration +
                '}';
    }
}