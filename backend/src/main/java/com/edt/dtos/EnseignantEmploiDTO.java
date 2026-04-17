package com.edt.dtos;

import java.time.LocalDate;  // ✅ IMPORT MANQUANT
import java.util.List;
import java.util.Map;

public class EnseignantEmploiDTO {
    private String enseignantId;
    private String enseignantNom;
    private String enseignantPrenom;
    private String anneeScolaire;
    private LocalDate dateGeneration;
    private Map<String, List<CoursEnseignantDTO>> emploiParJour;
    
    // Getters et setters
    public String getEnseignantId() { return enseignantId; }
    public void setEnseignantId(String enseignantId) { this.enseignantId = enseignantId; }
    
    public String getEnseignantNom() { return enseignantNom; }
    public void setEnseignantNom(String enseignantNom) { this.enseignantNom = enseignantNom; }
    
    public String getEnseignantPrenom() { return enseignantPrenom; }
    public void setEnseignantPrenom(String enseignantPrenom) { this.enseignantPrenom = enseignantPrenom; }
    
    public String getAnneeScolaire() { return anneeScolaire; }
    public void setAnneeScolaire(String anneeScolaire) { this.anneeScolaire = anneeScolaire; }
    
    public LocalDate getDateGeneration() { return dateGeneration; }
    public void setDateGeneration(LocalDate dateGeneration) { this.dateGeneration = dateGeneration; }
    
    public Map<String, List<CoursEnseignantDTO>> getEmploiParJour() { return emploiParJour; }
    public void setEmploiParJour(Map<String, List<CoursEnseignantDTO>> emploiParJour) { this.emploiParJour = emploiParJour; }
    
    public static class CoursEnseignantDTO {
        private String heure;
        private String matiere;
        private String classe;
        private String salle;
        
        public String getHeure() { return heure; }
        public void setHeure(String heure) { this.heure = heure; }
        
        public String getMatiere() { return matiere; }
        public void setMatiere(String matiere) { this.matiere = matiere; }
        
        public String getClasse() { return classe; }
        public void setClasse(String classe) { this.classe = classe; }
        
        public String getSalle() { return salle; }
        public void setSalle(String salle) { this.salle = salle; }
    }
}