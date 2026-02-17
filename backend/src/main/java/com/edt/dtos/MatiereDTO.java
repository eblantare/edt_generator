package com.edt.dtos;

public class MatiereDTO {
    private String id;
    private String code;
    private String nom;
    private String cycle;
    private String niveauClasse;
    
    // Constructeurs
    public MatiereDTO() {}
    
    public MatiereDTO(String id, String code, String nom, String cycle, String niveauClasse) {
        this.id = id;
        this.code = code;
        this.nom = nom;
        this.cycle = cycle;
        this.niveauClasse = niveauClasse;
    }
    
    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getCycle() { return cycle; }
    public void setCycle(String cycle) { this.cycle = cycle; }
    
    public String getNiveauClasse() { return niveauClasse; }
    public void setNiveauClasse(String niveauClasse) { this.niveauClasse = niveauClasse; }
    
    @Override
    public String toString() {
        return "MatiereDTO{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", nom='" + nom + '\'' +
                ", cycle='" + cycle + '\'' +
                ", niveauClasse='" + niveauClasse + '\'' +
                '}';
    }
}