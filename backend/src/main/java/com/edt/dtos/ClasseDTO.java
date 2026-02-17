// C:\projets\java\edt-generator\backend\src\main\java\com\edt\dtos\ClasseDTO.java
package com.edt.dtos;

public class ClasseDTO {
    private String id;
    private String nom;
    private String niveau;
    private String filiere;
    private Integer effectif;
    
    // Constructeurs
    public ClasseDTO() {}
    
    public ClasseDTO(String id, String nom, String niveau, String filiere, Integer effectif) {
        this.id = id;
        this.nom = nom;
        this.niveau = niveau;
        this.filiere = filiere;
        this.effectif = effectif;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    
    public String getFiliere() { return filiere; }
    public void setFiliere(String filiere) { this.filiere = filiere; }
    
    public Integer getEffectif() { return effectif; }
    public void setEffectif(Integer effectif) { this.effectif = effectif; }
}