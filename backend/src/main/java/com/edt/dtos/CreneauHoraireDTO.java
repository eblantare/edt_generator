package com.edt.dtos;

import java.time.LocalTime;

public class CreneauHoraireDTO {
    private String id;
    private String emploiDuTempsId;
    private String emploiDuTempsNom;
    private String jourSemaine;
    private String heureDebut;
    private String heureFin;
    private Integer numeroCreneau;
    private String enseignantId;
    private String enseignantNom;
    private String enseignantPrenom;
    private String classeId;
    private String classeNom;
    private String matiereId;
    private String matiereCode;
    private String matiereNom;
    private String salle;
    private Boolean estLibre;
    private String statutCreneau; // "OCCUPE", "LIBRE", "PAUSE", "RECREATION"

    // Constructeurs
    public CreneauHoraireDTO() {}

    public CreneauHoraireDTO(String id, String emploiDuTempsId, String jourSemaine, 
                            String heureDebut, String heureFin, Integer numeroCreneau) {
        this.id = id;
        this.emploiDuTempsId = emploiDuTempsId;
        this.jourSemaine = jourSemaine;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.numeroCreneau = numeroCreneau;
        this.estLibre = true;
        this.statutCreneau = "LIBRE";
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmploiDuTempsId() { return emploiDuTempsId; }
    public void setEmploiDuTempsId(String emploiDuTempsId) { this.emploiDuTempsId = emploiDuTempsId; }

    public String getEmploiDuTempsNom() { return emploiDuTempsNom; }
    public void setEmploiDuTempsNom(String emploiDuTempsNom) { this.emploiDuTempsNom = emploiDuTempsNom; }

    public String getJourSemaine() { return jourSemaine; }
    public void setJourSemaine(String jourSemaine) { this.jourSemaine = jourSemaine; }

    public String getHeureDebut() { return heureDebut; }
    public void setHeureDebut(String heureDebut) { this.heureDebut = heureDebut; }

    public String getHeureFin() { return heureFin; }
    public void setHeureFin(String heureFin) { this.heureFin = heureFin; }

    public Integer getNumeroCreneau() { return numeroCreneau; }
    public void setNumeroCreneau(Integer numeroCreneau) { this.numeroCreneau = numeroCreneau; }

    public String getEnseignantId() { return enseignantId; }
    public void setEnseignantId(String enseignantId) { this.enseignantId = enseignantId; }

    public String getEnseignantNom() { return enseignantNom; }
    public void setEnseignantNom(String enseignantNom) { this.enseignantNom = enseignantNom; }

    public String getEnseignantPrenom() { return enseignantPrenom; }
    public void setEnseignantPrenom(String enseignantPrenom) { this.enseignantPrenom = enseignantPrenom; }

    public String getClasseId() { return classeId; }
    public void setClasseId(String classeId) { this.classeId = classeId; }

    public String getClasseNom() { return classeNom; }
    public void setClasseNom(String classeNom) { this.classeNom = classeNom; }

    public String getMatiereId() { return matiereId; }
    public void setMatiereId(String matiereId) { this.matiereId = matiereId; }

    public String getMatiereCode() { return matiereCode; }
    public void setMatiereCode(String matiereCode) { this.matiereCode = matiereCode; }

    public String getMatiereNom() { return matiereNom; }
    public void setMatiereNom(String matiereNom) { this.matiereNom = matiereNom; }

    public String getSalle() { return salle; }
    public void setSalle(String salle) { this.salle = salle; }

    public Boolean getEstLibre() { return estLibre; }
    public void setEstLibre(Boolean estLibre) { this.estLibre = estLibre; }

    public String getStatutCreneau() { return statutCreneau; }
    public void setStatutCreneau(String statutCreneau) { this.statutCreneau = statutCreneau; }

    // Méthodes utilitaires
    public String getEnseignantComplet() {
        return enseignantNom + " " + (enseignantPrenom != null ? enseignantPrenom : "");
    }

    public String getMatiereComplet() {
        return matiereCode + " - " + matiereNom;
    }

    public String getDescriptionCreneau() {
        if (estLibre || matiereId == null) {
            return "Créneau libre";
        }
        return matiereCode + " (" + enseignantNom + ") - " + classeNom;
    }

    public LocalTime getHeureDebutAsLocalTime() {
        return heureDebut != null ? LocalTime.parse(heureDebut) : null;
    }

    public LocalTime getHeureFinAsLocalTime() {
        return heureFin != null ? LocalTime.parse(heureFin) : null;
    }

    @Override
    public String toString() {
        return "CreneauHoraireDTO{" +
                "jourSemaine='" + jourSemaine + '\'' +
                ", heureDebut='" + heureDebut + '\'' +
                ", heureFin='" + heureFin + '\'' +
                ", enseignant='" + getEnseignantComplet() + '\'' +
                ", matiere='" + getMatiereComplet() + '\'' +
                ", classe='" + classeNom + '\'' +
                ", estLibre=" + estLibre +
                '}';
    }
}