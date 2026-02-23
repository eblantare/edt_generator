// C:\projets\java\edt-generator\backend\src\main\java\com\edt\entities\CodeConnexion.java
package com.edt.entities;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "codes_connexion")
public class CodeConnexion {
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;
    
    @Column(name = "code", nullable = false, length = 9)
    private String code; // Code à 7-9 chiffres
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;
    
    @Column(name = "est_utilise")
    private Boolean estUtilise = false;
    
    @Column(name = "tentatives")
    private Integer tentatives = 0;
    
    public CodeConnexion() {
        this.id = UUID.randomUUID().toString();
        this.dateCreation = LocalDateTime.now();
        this.dateExpiration = LocalDateTime.now().plusMinutes(10); // Code valable 10 minutes
    }
    
    public CodeConnexion(Utilisateur utilisateur, String code) {
        this();
        this.utilisateur = utilisateur;
        this.code = code;
    }
    
    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public LocalDateTime getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDateTime dateExpiration) { this.dateExpiration = dateExpiration; }
    
    public Boolean getEstUtilise() { return estUtilise; }
    public void setEstUtilise(Boolean estUtilise) { this.estUtilise = estUtilise; }
    
    public Integer getTentatives() { return tentatives; }
    public void setTentatives(Integer tentatives) { this.tentatives = tentatives; }
    
    public boolean estValide() {
        return !estUtilise && LocalDateTime.now().isBefore(dateExpiration) && tentatives < 3;
    }
}