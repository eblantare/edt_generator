package com.edt.dtos;

import java.util.List;

public class HoraireJournalierDTO {
    private String id;
    private String jourSemaine;
    private String heureDebutMatin;
    private String heureFinMatin;
    private String heureDebutApresMidi;
    private String heureFinApresMidi;
    private Boolean estJourCours;
    private Integer dureeCreneauMinutes;
    private Integer nombreCreneaux;
    private List<String> creneaux;
    
    // Constructeurs
    public HoraireJournalierDTO() {}
    
    public HoraireJournalierDTO(String id, String jourSemaine, Boolean estJourCours) {
        this.id = id;
        this.jourSemaine = jourSemaine;
        this.estJourCours = estJourCours;
    }
    
    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getJourSemaine() { return jourSemaine; }
    public void setJourSemaine(String jourSemaine) { this.jourSemaine = jourSemaine; }
    
    public String getHeureDebutMatin() { return heureDebutMatin; }
    public void setHeureDebutMatin(String heureDebutMatin) { this.heureDebutMatin = heureDebutMatin; }
    
    public String getHeureFinMatin() { return heureFinMatin; }
    public void setHeureFinMatin(String heureFinMatin) { this.heureFinMatin = heureFinMatin; }
    
    public String getHeureDebutApresMidi() { return heureDebutApresMidi; }
    public void setHeureDebutApresMidi(String heureDebutApresMidi) { this.heureDebutApresMidi = heureDebutApresMidi; }
    
    public String getHeureFinApresMidi() { return heureFinApresMidi; }
    public void setHeureFinApresMidi(String heureFinApresMidi) { this.heureFinApresMidi = heureFinApresMidi; }
    
    public Boolean getEstJourCours() { return estJourCours; }
    public void setEstJourCours(Boolean estJourCours) { this.estJourCours = estJourCours; }
    
    public Integer getDureeCreneauMinutes() { return dureeCreneauMinutes; }
    public void setDureeCreneauMinutes(Integer dureeCreneauMinutes) { this.dureeCreneauMinutes = dureeCreneauMinutes; }
    
    public Integer getNombreCreneaux() { return nombreCreneaux; }
    public void setNombreCreneaux(Integer nombreCreneaux) { this.nombreCreneaux = nombreCreneaux; }
    
    public List<String> getCreneaux() { return creneaux; }
    public void setCreneaux(List<String> creneaux) { this.creneaux = creneaux; }
    
    // Méthodes utilitaires pour compatibilité
    public boolean isEstJourCours() { 
        return estJourCours != null ? estJourCours : false; 
    }
    
    public boolean isJourCours() { 
        return estJourCours != null ? estJourCours : false; 
    }
    
    public void setJourCours(boolean jourCours) {
        this.estJourCours = jourCours;
    }
}