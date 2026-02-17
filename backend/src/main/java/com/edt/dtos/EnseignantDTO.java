package com.edt.dtos;

public class EnseignantDTO {
    private String id;
    private String nom;
    private String prenom;
    private String matricule;
    private String email;
    private String telephone;
    private Integer heuresMaxHebdo;
    private MatiereDTO matiereDominante;
    private MatiereDTO matiereSecondaire;
    
    // Constructeurs
    public EnseignantDTO() {}
    
    public EnseignantDTO(String id, String nom, String prenom, String matricule, 
                        String email, String telephone, Integer heuresMaxHebdo) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.matricule = matricule;
        this.email = email;
        this.telephone = telephone;
        this.heuresMaxHebdo = heuresMaxHebdo;
    }
    
    // Getters et Setters
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
    
    public MatiereDTO getMatiereDominante() { return matiereDominante; }
    public void setMatiereDominante(MatiereDTO matiereDominante) { this.matiereDominante = matiereDominante; }
    
    public MatiereDTO getMatiereSecondaire() { return matiereSecondaire; }
    public void setMatiereSecondaire(MatiereDTO matiereSecondaire) { this.matiereSecondaire = matiereSecondaire; }
}