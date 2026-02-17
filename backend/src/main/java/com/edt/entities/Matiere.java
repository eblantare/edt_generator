package com.edt.entities;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "matieres")
public class Matiere {
    @Id
    private String id;
    
    @Column(unique = true, nullable = false)
    private String code;
    
    @Column(nullable = false)
    private String nom;
    
    @Column(name = "cycle", nullable = false)
    private String cycle; // "college", "lycee", "lycee_tech", "lycee_pro", "bt"
    
    @Column(name = "niveau_classe")
    private String niveauClasse; // Optionnel : "6ème", "1ère", etc.
    
    // Constructeurs
    public Matiere() {
        this.id = UUID.randomUUID().toString();
    }
    
    public Matiere(String code, String nom, String cycle, String niveauClasse) {
        this();
        this.code = code;
        this.nom = nom;
        this.cycle = cycle;
        this.niveauClasse = niveauClasse;
    }
    
    // Getters and Setters
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
}