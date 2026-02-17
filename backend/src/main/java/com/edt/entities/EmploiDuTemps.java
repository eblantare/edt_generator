package com.edt.entities;

import javax.persistence.*;
import java.time.LocalDate;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "emplois_du_temps")
public class EmploiDuTemps {
    
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
    
    @Column(name = "annee_scolaire")
    private String anneeScolaire;
    
    @Column(name = "date_generation")
    private LocalDate dateGeneration;
    
    @Column(name = "est_global")
    private Boolean estGlobal;
    
    private String statut;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", nullable = true)
    private Classe classe;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_id", nullable = true)
    private Enseignant enseignant;
    
    @Column(name = "created_at")
    private LocalDate createdAt;
    
    @Column(name = "updated_at")
    private LocalDate updatedAt;
    
    // Constructeur par défaut
    public EmploiDuTemps() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
        this.estGlobal = false;
        this.statut = "EN_COURS";
    }
    
    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getAnneeScolaire() { return anneeScolaire; }
    public void setAnneeScolaire(String anneeScolaire) { this.anneeScolaire = anneeScolaire; }
    
    public LocalDate getDateGeneration() { return dateGeneration; }
    public void setDateGeneration(LocalDate dateGeneration) { this.dateGeneration = dateGeneration; }
    
    public Boolean getEstGlobal() { return estGlobal; }
    public void setEstGlobal(Boolean estGlobal) { this.estGlobal = estGlobal; }
    
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    
    public Classe getClasse() { return classe; }
    public void setClasse(Classe classe) { this.classe = classe; }
    
    public Enseignant getEnseignant() { return enseignant; }
    public void setEnseignant(Enseignant enseignant) { this.enseignant = enseignant; }
    
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
    
    public LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDate updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDate.now();
    }
}