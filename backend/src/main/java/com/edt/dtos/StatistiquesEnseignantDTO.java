package com.edt.dtos;

import java.util.Map;

public class StatistiquesEnseignantDTO {
    private String enseignantId;
    private String nomEnseignant;
    private int totalHeures;
    private Map<String, Long> heuresParJour;
    private Map<String, Long> heuresParMatiere;
    
    // Getters et Setters
    public String getEnseignantId() { return enseignantId; }
    public void setEnseignantId(String enseignantId) { this.enseignantId = enseignantId; }
    
    public String getNomEnseignant() { return nomEnseignant; }
    public void setNomEnseignant(String nomEnseignant) { this.nomEnseignant = nomEnseignant; }
    
    public int getTotalHeures() { return totalHeures; }
    public void setTotalHeures(int totalHeures) { this.totalHeures = totalHeures; }
    
    public Map<String, Long> getHeuresParJour() { return heuresParJour; }
    public void setHeuresParJour(Map<String, Long> heuresParJour) { this.heuresParJour = heuresParJour; }
    
    public Map<String, Long> getHeuresParMatiere() { return heuresParMatiere; }
    public void setHeuresParMatiere(Map<String, Long> heuresParMatiere) { this.heuresParMatiere = heuresParMatiere; }
}