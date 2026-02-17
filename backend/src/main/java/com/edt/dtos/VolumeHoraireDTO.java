package com.edt.dtos;

public class VolumeHoraireDTO {
    private String id;
    private String classeId;
    private String classeNom;
    private String matiereId;
    private String matiereNom;
    private int volumeHoraireAnnuel;
    private int volumeHoraireSemestriel;
    private int volumeHoraireHebdomadaire;
    
    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getClasseId() { return classeId; }
    public void setClasseId(String classeId) { this.classeId = classeId; }
    
    public String getClasseNom() { return classeNom; }
    public void setClasseNom(String classeNom) { this.classeNom = classeNom; }
    
    public String getMatiereId() { return matiereId; }
    public void setMatiereId(String matiereId) { this.matiereId = matiereId; }
    
    public String getMatiereNom() { return matiereNom; }
    public void setMatiereNom(String matiereNom) { this.matiereNom = matiereNom; }
    
    public int getVolumeHoraireAnnuel() { return volumeHoraireAnnuel; }
    public void setVolumeHoraireAnnuel(int volumeHoraireAnnuel) { this.volumeHoraireAnnuel = volumeHoraireAnnuel; }
    
    public int getVolumeHoraireSemestriel() { return volumeHoraireSemestriel; }
    public void setVolumeHoraireSemestriel(int volumeHoraireSemestriel) { this.volumeHoraireSemestriel = volumeHoraireSemestriel; }
    
    public int getVolumeHoraireHebdomadaire() { return volumeHoraireHebdomadaire; }
    public void setVolumeHoraireHebdomadaire(int volumeHoraireHebdomadaire) { this.volumeHoraireHebdomadaire = volumeHoraireHebdomadaire; }
}