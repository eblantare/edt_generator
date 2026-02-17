package com.edt.dtos;

import java.time.LocalDate;
import java.util.List;

public class GenerationStatutDTO{
    private String id;
    private String nom;
    private String statut;
    private LocalDate dateGeneration;
    private LocalDate dateDerniereModification;
    private int totalCreneaux;
    private int creneauxOccupes;
    private int creneauxLibres;
    private double tauxOccupation;
    private int progression;
    private boolean avecConflits;
    private List<String> messages;

    public GenerationStatutDTO() {}

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    
    public LocalDate getDateGeneration() { return dateGeneration; }
    public void setDateGeneration(LocalDate dateGeneration) { this.dateGeneration = dateGeneration; }
    
    public LocalDate getDateDerniereModification() { return dateDerniereModification; }
    public void setDateDerniereModification(LocalDate dateDerniereModification) { this.dateDerniereModification = dateDerniereModification; }
    
    public int getTotalCreneaux() { return totalCreneaux; }
    public void setTotalCreneaux(int totalCreneaux) { this.totalCreneaux = totalCreneaux; }
    
    public int getCreneauxOccupes() { return creneauxOccupes; }
    public void setCreneauxOccupes(int creneauxOccupes) { this.creneauxOccupes = creneauxOccupes; }
    
    public int getCreneauxLibres() { return creneauxLibres; }
    public void setCreneauxLibres(int creneauxLibres) { this.creneauxLibres = creneauxLibres; }
    
    public double getTauxOccupation() { return tauxOccupation; }
    public void setTauxOccupation(double tauxOccupation) { this.tauxOccupation = tauxOccupation; }
    
    public int getProgression() { return progression; }
    public void setProgression(int progression) { this.progression = progression; }
    
    public boolean isAvecConflits() { return avecConflits; }
    public void setAvecConflits(boolean avecConflits) { this.avecConflits = avecConflits; }
    
    public List<String> getMessages() { return messages; }
    public void setMessages(List<String> messages) { this.messages = messages; }
}