package com.edt.dtos;

import java.time.LocalDateTime;

public class AffectationCreneauDTO {
    private String id;
    private String creneauId;
    private String creneauJourSemaine;
    private String creneauHeureDebut;
    private String creneauHeureFin;
    private Integer creneauNumero;
    
    private String enseignantId;
    private String enseignantNom;
    private String enseignantPrenom;
    private String enseignantMatricule;
    
    private String classeId;
    private String classeNom;
    private String classeNiveau;
    
    private String matiereId;
    private String matiereCode;
    private String matiereNom;
    
    private String salle;
    private String typeAffectation; // "COURS", "PERMANENCE", "REUNION", "AUTRE"
    private String statutAffectation; // "PROVISOIRE", "CONFIRME", "ANNULE"
    
    private LocalDateTime dateAffectation;
    private String utilisateurAffectationId;
    private String utilisateurAffectationNom;
    
    private LocalDateTime dateConfirmation;
    private String utilisateurConfirmationId;
    private String utilisateurConfirmationNom;
    
    private String commentaire;
    private Boolean forceAffectation; // Pour forcer même si conflit
    private Boolean remplacerAffectationExistante; // Remplacer l'affectation existante
    
    // Pour les conflits détectés
    private Boolean conflitDetecte;
    private String typeConflit; // "ENSEIGNANT", "CLASSE", "SALLE", "MATIERE"
    private String descriptionConflit;
    private String creneauConflitId;
    private String resolutionConflit; // "IGNORER", "DECALER", "ANNULER_AUTRE"

    // Constructeurs
    public AffectationCreneauDTO() {
        this.dateAffectation = LocalDateTime.now();
        this.statutAffectation = "PROVISOIRE";
        this.typeAffectation = "COURS";
        this.forceAffectation = false;
        this.remplacerAffectationExistante = false;
        this.conflitDetecte = false;
    }

    public AffectationCreneauDTO(String creneauId, String enseignantId, String classeId, String matiereId) {
        this();
        this.creneauId = creneauId;
        this.enseignantId = enseignantId;
        this.classeId = classeId;
        this.matiereId = matiereId;
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCreneauId() { return creneauId; }
    public void setCreneauId(String creneauId) { this.creneauId = creneauId; }

    public String getCreneauJourSemaine() { return creneauJourSemaine; }
    public void setCreneauJourSemaine(String creneauJourSemaine) { this.creneauJourSemaine = creneauJourSemaine; }

    public String getCreneauHeureDebut() { return creneauHeureDebut; }
    public void setCreneauHeureDebut(String creneauHeureDebut) { this.creneauHeureDebut = creneauHeureDebut; }

    public String getCreneauHeureFin() { return creneauHeureFin; }
    public void setCreneauHeureFin(String creneauHeureFin) { this.creneauHeureFin = creneauHeureFin; }

    public Integer getCreneauNumero() { return creneauNumero; }
    public void setCreneauNumero(Integer creneauNumero) { this.creneauNumero = creneauNumero; }

    public String getEnseignantId() { return enseignantId; }
    public void setEnseignantId(String enseignantId) { this.enseignantId = enseignantId; }

    public String getEnseignantNom() { return enseignantNom; }
    public void setEnseignantNom(String enseignantNom) { this.enseignantNom = enseignantNom; }

    public String getEnseignantPrenom() { return enseignantPrenom; }
    public void setEnseignantPrenom(String enseignantPrenom) { this.enseignantPrenom = enseignantPrenom; }

    public String getEnseignantMatricule() { return enseignantMatricule; }
    public void setEnseignantMatricule(String enseignantMatricule) { this.enseignantMatricule = enseignantMatricule; }

    public String getClasseId() { return classeId; }
    public void setClasseId(String classeId) { this.classeId = classeId; }

    public String getClasseNom() { return classeNom; }
    public void setClasseNom(String classeNom) { this.classeNom = classeNom; }

    public String getClasseNiveau() { return classeNiveau; }
    public void setClasseNiveau(String classeNiveau) { this.classeNiveau = classeNiveau; }

    public String getMatiereId() { return matiereId; }
    public void setMatiereId(String matiereId) { this.matiereId = matiereId; }

    public String getMatiereCode() { return matiereCode; }
    public void setMatiereCode(String matiereCode) { this.matiereCode = matiereCode; }

    public String getMatiereNom() { return matiereNom; }
    public void setMatiereNom(String matiereNom) { this.matiereNom = matiereNom; }

    public String getSalle() { return salle; }
    public void setSalle(String salle) { this.salle = salle; }

    public String getTypeAffectation() { return typeAffectation; }
    public void setTypeAffectation(String typeAffectation) { this.typeAffectation = typeAffectation; }

    public String getStatutAffectation() { return statutAffectation; }
    public void setStatutAffectation(String statutAffectation) { this.statutAffectation = statutAffectation; }

    public LocalDateTime getDateAffectation() { return dateAffectation; }
    public void setDateAffectation(LocalDateTime dateAffectation) { this.dateAffectation = dateAffectation; }

    public String getUtilisateurAffectationId() { return utilisateurAffectationId; }
    public void setUtilisateurAffectationId(String utilisateurAffectationId) { this.utilisateurAffectationId = utilisateurAffectationId; }

    public String getUtilisateurAffectationNom() { return utilisateurAffectationNom; }
    public void setUtilisateurAffectationNom(String utilisateurAffectationNom) { this.utilisateurAffectationNom = utilisateurAffectationNom; }

    public LocalDateTime getDateConfirmation() { return dateConfirmation; }
    public void setDateConfirmation(LocalDateTime dateConfirmation) { this.dateConfirmation = dateConfirmation; }

    public String getUtilisateurConfirmationId() { return utilisateurConfirmationId; }
    public void setUtilisateurConfirmationId(String utilisateurConfirmationId) { this.utilisateurConfirmationId = utilisateurConfirmationId; }

    public String getUtilisateurConfirmationNom() { return utilisateurConfirmationNom; }
    public void setUtilisateurConfirmationNom(String utilisateurConfirmationNom) { this.utilisateurConfirmationNom = utilisateurConfirmationNom; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public Boolean getForceAffectation() { return forceAffectation; }
    public void setForceAffectation(Boolean forceAffectation) { this.forceAffectation = forceAffectation; }

    public Boolean getRemplacerAffectationExistante() { return remplacerAffectationExistante; }
    public void setRemplacerAffectationExistante(Boolean remplacerAffectationExistante) { this.remplacerAffectationExistante = remplacerAffectationExistante; }

    public Boolean getConflitDetecte() { return conflitDetecte; }
    public void setConflitDetecte(Boolean conflitDetecte) { this.conflitDetecte = conflitDetecte; }

    public String getTypeConflit() { return typeConflit; }
    public void setTypeConflit(String typeConflit) { this.typeConflit = typeConflit; }

    public String getDescriptionConflit() { return descriptionConflit; }
    public void setDescriptionConflit(String descriptionConflit) { this.descriptionConflit = descriptionConflit; }

    public String getCreneauConflitId() { return creneauConflitId; }
    public void setCreneauConflitId(String creneauConflitId) { this.creneauConflitId = creneauConflitId; }

    public String getResolutionConflit() { return resolutionConflit; }
    public void setResolutionConflit(String resolutionConflit) { this.resolutionConflit = resolutionConflit; }

    // Méthodes utilitaires
    public String getEnseignantComplet() {
        return enseignantNom + " " + (enseignantPrenom != null ? enseignantPrenom : "");
    }

    public String getClasseComplete() {
        return classeNom + (classeNiveau != null ? " (" + classeNiveau + ")" : "");
    }

    public String getMatiereComplete() {
        return matiereCode + " - " + matiereNom;
    }

    public String getDescriptionAffectation() {
        StringBuilder sb = new StringBuilder();
        sb.append(getEnseignantComplet())
          .append(" → ")
          .append(getClasseComplete())
          .append(" → ")
          .append(getMatiereComplete());
        
        if (salle != null) {
            sb.append(" [Salle: ").append(salle).append("]");
        }
        
        return sb.toString();
    }

    public String getCreneauComplet() {
        return creneauJourSemaine + " " + creneauHeureDebut + "-" + creneauHeureFin;
    }

    public Boolean estConfirme() {
        return "CONFIRME".equals(statutAffectation);
    }

    public Boolean estAnnule() {
        return "ANNULE".equals(statutAffectation);
    }

    public Boolean estProvisoire() {
        return "PROVISOIRE".equals(statutAffectation);
    }

    public String getStatutCouleur() {
        switch (statutAffectation) {
            case "CONFIRME": return "success";
            case "PROVISOIRE": return "warning";
            case "ANNULE": return "danger";
            default: return "secondary";
        }
    }

    public String getStatutIcone() {
        switch (statutAffectation) {
            case "CONFIRME": return "bi-check-circle";
            case "PROVISOIRE": return "bi-clock";
            case "ANNULE": return "bi-x-circle";
            default: return "bi-question-circle";
        }
    }

    public Boolean aConflitEnseignant() {
        return conflitDetecte && "ENSEIGNANT".equals(typeConflit);
    }

    public Boolean aConflitClasse() {
        return conflitDetecte && "CLASSE".equals(typeConflit);
    }

    public Boolean aConflitSalle() {
        return conflitDetecte && "SALLE".equals(typeConflit);
    }

    public Boolean estCours() {
        return "COURS".equals(typeAffectation);
    }

    public Boolean peutEtreConfirme() {
        return !conflitDetecte || forceAffectation || "IGNORER".equals(resolutionConflit);
    }

    @Override
    public String toString() {
        return "AffectationCreneauDTO{" +
                "creneau='" + getCreneauComplet() + '\'' +
                ", enseignant='" + getEnseignantComplet() + '\'' +
                ", classe='" + classeNom + '\'' +
                ", matiere='" + matiereCode + '\'' +
                ", statut='" + statutAffectation + '\'' +
                ", conflit=" + conflitDetecte +
                '}';
    }
}