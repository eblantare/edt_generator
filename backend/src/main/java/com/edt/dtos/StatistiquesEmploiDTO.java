package com.edt.dtos;

public class StatistiquesEmploiDTO {
    private String emploiId;
    private String nomEmploi;
    private int totalCreneaux;
    private int creneauxOccupes;
    private int creneauxLibres;
    private double tauxOccupation;
    
    // Getters et Setters
    public String getEmploiId() { return emploiId; }
    public void setEmploiId(String emploiId) { this.emploiId = emploiId; }
    
    public String getNomEmploi() { return nomEmploi; }
    public void setNomEmploi(String nomEmploi) { this.nomEmploi = nomEmploi; }
    
    public int getTotalCreneaux() { return totalCreneaux; }
    public void setTotalCreneaux(int totalCreneaux) { this.totalCreneaux = totalCreneaux; }
    
    public int getCreneauxOccupes() { return creneauxOccupes; }
    public void setCreneauxOccupes(int creneauxOccupes) { this.creneauxOccupes = creneauxOccupes; }
    
    public int getCreneauxLibres() { return creneauxLibres; }
    public void setCreneauxLibres(int creneauxLibres) { this.creneauxLibres = creneauxLibres; }
    
    public double getTauxOccupation() { return tauxOccupation; }
    public void setTauxOccupation(double tauxOccupation) { this.tauxOccupation = tauxOccupation; }
}