package com.edt.dtos;

import com.edt.entities.Matiere;

public class EnseignantDTO {
    private String id;
    private String nom;
    private String prenom;
    private String matricule;
    private String email;
    private String telephone;
    private Integer heuresMaxHebdo;
    
    // ✅ AJOUT : Champs pour les matières
    private String matiereDominanteId;
    private Matiere matiereDominante;
    
    private String matiereSecondaireId;
    private Matiere matiereSecondaire;
    
    // Constructeurs
    public EnseignantDTO() {}
    
    // Getters et Setters existants
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    
    public Integer getHeuresMaxHebdo() { return heuresMaxHebdo; }
    public void setHeuresMaxHebdo(Integer heuresMaxHebdo) { this.heuresMaxHebdo = heuresMaxHebdo; }
    
    // ✅ NOUVEAUX Getters/Setters pour les matières
    public String getMatiereDominanteId() { return matiereDominanteId; }
    public void setMatiereDominanteId(String matiereDominanteId) { this.matiereDominanteId = matiereDominanteId; }
    
    public Matiere getMatiereDominante() { return matiereDominante; }
    public void setMatiereDominante(Matiere matiereDominante) { 
        this.matiereDominante = matiereDominante;
        if (matiereDominante != null) {
            this.matiereDominanteId = matiereDominante.getId();
        }
    }
    
    public String getMatiereSecondaireId() { return matiereSecondaireId; }
    public void setMatiereSecondaireId(String matiereSecondaireId) { this.matiereSecondaireId = matiereSecondaireId; }
    
    public Matiere getMatiereSecondaire() { return matiereSecondaire; }
    public void setMatiereSecondaire(Matiere matiereSecondaire) { 
        this.matiereSecondaire = matiereSecondaire;
        if (matiereSecondaire != null) {
            this.matiereSecondaireId = matiereSecondaire.getId();
        }
    }
}