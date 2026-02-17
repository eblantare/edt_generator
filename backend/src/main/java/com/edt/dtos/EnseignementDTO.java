package com.edt.dtos;

public class EnseignementDTO {
    private String id;
    private String enseignantId;
    private String enseignantMatricule;
    private String enseignantNom;
    private String enseignantPrenom;
    private String classeId;
    private String classeNom;
    private String classeNiveau;
    private String classeFiliere;
    private Integer classeEffectif;
    private String matiereId;
    private String matiereCode;
    private String matiereNom;
    private String matiereCycle;
    private Integer heuresParSemaine;
    private Integer heuresAttribuees;
    private Integer heuresRestantes;
    private Boolean estMatiereDominante;
    private String statut; // "ACTIF", "INACTIF", "PROVISOIRE"
    private String commentaire;
    private Integer ordrePriorite; // 1=haute priorité, 3=basse priorité

    // Constructeurs
    public EnseignementDTO() {}

    public EnseignementDTO(String id, String enseignantId, String classeId, String matiereId, 
                          Integer heuresParSemaine) {
        this.id = id;
        this.enseignantId = enseignantId;
        this.classeId = classeId;
        this.matiereId = matiereId;
        this.heuresParSemaine = heuresParSemaine;
        this.heuresAttribuees = 0;
        this.heuresRestantes = heuresParSemaine;
        this.statut = "ACTIF";
        this.ordrePriorite = 2; // Priorité moyenne par défaut
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEnseignantId() { return enseignantId; }
    public void setEnseignantId(String enseignantId) { this.enseignantId = enseignantId; }

    public String getEnseignantMatricule() { return enseignantMatricule; }
    public void setEnseignantMatricule(String enseignantMatricule) { this.enseignantMatricule = enseignantMatricule; }

    public String getEnseignantNom() { return enseignantNom; }
    public void setEnseignantNom(String enseignantNom) { this.enseignantNom = enseignantNom; }

    public String getEnseignantPrenom() { return enseignantPrenom; }
    public void setEnseignantPrenom(String enseignantPrenom) { this.enseignantPrenom = enseignantPrenom; }

    public String getClasseId() { return classeId; }
    public void setClasseId(String classeId) { this.classeId = classeId; }

    public String getClasseNom() { return classeNom; }
    public void setClasseNom(String classeNom) { this.classeNom = classeNom; }

    public String getClasseNiveau() { return classeNiveau; }
    public void setClasseNiveau(String classeNiveau) { this.classeNiveau = classeNiveau; }

    public String getClasseFiliere() { return classeFiliere; }
    public void setClasseFiliere(String classeFiliere) { this.classeFiliere = classeFiliere; }

    public Integer getClasseEffectif() { return classeEffectif; }
    public void setClasseEffectif(Integer classeEffectif) { this.classeEffectif = classeEffectif; }

    public String getMatiereId() { return matiereId; }
    public void setMatiereId(String matiereId) { this.matiereId = matiereId; }

    public String getMatiereCode() { return matiereCode; }
    public void setMatiereCode(String matiereCode) { this.matiereCode = matiereCode; }

    public String getMatiereNom() { return matiereNom; }
    public void setMatiereNom(String matiereNom) { this.matiereNom = matiereNom; }

    public String getMatiereCycle() { return matiereCycle; }
    public void setMatiereCycle(String matiereCycle) { this.matiereCycle = matiereCycle; }

    public Integer getHeuresParSemaine() { return heuresParSemaine; }
    public void setHeuresParSemaine(Integer heuresParSemaine) { 
        this.heuresParSemaine = heuresParSemaine;
        this.calculerHeuresRestantes();
    }

    public Integer getHeuresAttribuees() { return heuresAttribuees; }
    public void setHeuresAttribuees(Integer heuresAttribuees) { 
        this.heuresAttribuees = heuresAttribuees;
        this.calculerHeuresRestantes();
    }

    public Integer getHeuresRestantes() { return heuresRestantes; }
    public void setHeuresRestantes(Integer heuresRestantes) { this.heuresRestantes = heuresRestantes; }

    public Boolean getEstMatiereDominante() { return estMatiereDominante; }
    public void setEstMatiereDominante(Boolean estMatiereDominante) { this.estMatiereDominante = estMatiereDominante; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public Integer getOrdrePriorite() { return ordrePriorite; }
    public void setOrdrePriorite(Integer ordrePriorite) { this.ordrePriorite = ordrePriorite; }

    // Méthodes utilitaires
    private void calculerHeuresRestantes() {
        if (heuresParSemaine != null && heuresAttribuees != null) {
            this.heuresRestantes = Math.max(0, heuresParSemaine - heuresAttribuees);
        }
    }

    public String getEnseignantComplet() {
        return enseignantNom + " " + (enseignantPrenom != null ? enseignantPrenom : "");
    }

    public String getClasseComplete() {
        StringBuilder sb = new StringBuilder();
        sb.append(classeNom);
        if (classeNiveau != null) {
            sb.append(" (").append(classeNiveau).append(")");
        }
        return sb.toString();
    }

    public String getMatiereComplete() {
        return matiereCode + " - " + matiereNom;
    }

    public String getDescriptionComplete() {
        return getEnseignantComplet() + " → " + getClasseComplete() + " → " + getMatiereComplete();
    }

    public Double getPourcentageAttribution() {
        if (heuresParSemaine == null || heuresParSemaine == 0) {
            return 0.0;
        }
        if (heuresAttribuees == null) {
            return 0.0;
        }
        return (heuresAttribuees * 100.0) / heuresParSemaine;
    }

    public String getStatutAttribution() {
        Double pourcentage = getPourcentageAttribution();
        if (pourcentage == 0) {
            return "Non attribué";
        } else if (pourcentage < 100) {
            return "Partiellement attribué";
        } else {
            return "Complètement attribué";
        }
    }

    public String getStatutCouleur() {
        Double pourcentage = getPourcentageAttribution();
        if (pourcentage == 0) {
            return "danger";
        } else if (pourcentage < 100) {
            return "warning";
        } else {
            return "success";
        }
    }

    public String getTypeMatiere() {
        return estMatiereDominante != null && estMatiereDominante ? "Dominante" : "Secondaire";
    }

    public Boolean estCompletementAttribue() {
        return heuresParSemaine != null && heuresAttribuees != null && 
               heuresAttribuees >= heuresParSemaine;
    }

    public Boolean peutAttribuerHeures(Integer heures) {
        if (heuresParSemaine == null || heuresRestantes == null) {
            return false;
        }
        return heures != null && heures > 0 && heures <= heuresRestantes;
    }

    @Override
    public String toString() {
        return "EnseignementDTO{" +
                "enseignant='" + getEnseignantComplet() + '\'' +
                ", classe='" + classeNom + '\'' +
                ", matiere='" + matiereCode + '\'' +
                ", heures=" + heuresParSemaine + "/" + heuresAttribuees +
                ", statut='" + statut + '\'' +
                '}';
    }
}