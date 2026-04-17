package com.edt.dtos;

import java.time.LocalDate;

public class GenerationHistoriqueDTO {
    private String id;
    private String nom;
    private String anneeScolaire;
    private LocalDate dateGeneration;
    private String statut;
    private String classeNom;
    private String enseignantId;    // ✅ AJOUT
    private String enseignantNom;   // ✅ AJOUT
    private String type;
    private int totalCreneaux;
    private int creneauxOccupes;
    
    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getAnneeScolaire() { return anneeScolaire; }
    public void setAnneeScolaire(String anneeScolaire) { this.anneeScolaire = anneeScolaire; }
    
    public LocalDate getDateGeneration() { return dateGeneration; }
    public void setDateGeneration(LocalDate dateGeneration) { this.dateGeneration = dateGeneration; }
    
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    
    public String getClasseNom() { return classeNom; }
    public void setClasseNom(String classeNom) { this.classeNom = classeNom; }
    
    public String getEnseignantId() { return enseignantId; }        // ✅ AJOUT
    public void setEnseignantId(String enseignantId) { this.enseignantId = enseignantId; }  // ✅ AJOUT
    
    public String getEnseignantNom() { return enseignantNom; }      // ✅ AJOUT
    public void setEnseignantNom(String enseignantNom) { this.enseignantNom = enseignantNom; }  // ✅ AJOUT
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public int getTotalCreneaux() { return totalCreneaux; }
    public void setTotalCreneaux(int totalCreneaux) { this.totalCreneaux = totalCreneaux; }
    
    public int getCreneauxOccupes() { return creneauxOccupes; }
    public void setCreneauxOccupes(int creneauxOccupes) { this.creneauxOccupes = creneauxOccupes; }
}