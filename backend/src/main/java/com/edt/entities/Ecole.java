package com.edt.entities;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "ecoles")
public class Ecole {
    
     @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false)
    private String id;
    
    @Column(nullable = false)
    private String nom;
    
    private String telephone;
    
    @Column(length = 500)
    private String adresse;
    
    @Column(length = 1000)
    private String logo; // URL ou chemin du logo
    
    private String devise;
    
    @Column(name = "dre")
    private String dre; // Direction Régionale de l'Education
    
    @Column(name = "iesg")
    private String iesg; // Inspection de l'enseignement général du secondaire
    
    @Column(name = "bp")
    private String bp; // Boîte Postale
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructeurs
    public Ecole() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
    
    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }
    
    public String getDre() { return dre; }
    public void setDre(String dre) { this.dre = dre; }
    
    public String getIesg() { return iesg; }
    public void setIesg(String iesg) { this.iesg = iesg; }
    
    public String getBp() { return bp; }
    public void setBp(String bp) { this.bp = bp; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}