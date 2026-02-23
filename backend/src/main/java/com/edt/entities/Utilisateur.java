// C:\projets\java\edt-generator\backend\src\main\java\com\edt\entities\Utilisateur.java
package com.edt.entities;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "utilisateurs")
public class Utilisateur {
    @Id
    private String id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    // SUPPRIMÉ : private String motDePasse;
    
    @Column(name = "est_actif")
    private Boolean estActif = true;
    
    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;
    
    @Column(name = "role")
    private String role = "ADMIN"; // ADMIN, GESTIONNAIRE, CONSULTANT
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructeurs
    public Utilisateur() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Constructeur sans mot de passe
    public Utilisateur(String email) {
        this();
        this.email = email;
    }
    
    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    // SUPPRIMÉ : getMotDePasse() et setMotDePasse()
    
    public Boolean getEstActif() { return estActif; }
    public void setEstActif(Boolean estActif) { this.estActif = estActif; }
    
    public LocalDateTime getDerniereConnexion() { return derniereConnexion; }
    public void setDerniereConnexion(LocalDateTime derniereConnexion) { this.derniereConnexion = derniereConnexion; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}