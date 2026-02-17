package com.edt.entities;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "enseignants")
public class Enseignant {
    @Id
    private String id;
    
    private String nom;
    private String prenom;
    private String matricule;
    private String email;
    private String telephone;
    
    @OneToMany(mappedBy = "enseignant", cascade = CascadeType.ALL)
    private Set<Enseignement> enseignements = new HashSet<>();
    
    @OneToMany(mappedBy = "enseignant")
    private Set<Disponibilite> disponibilites = new HashSet<>();
    
    private Integer heuresMaxHebdo;
    
    // OPTIONNEL: Ajouter des relations directes avec Matiere pour simplifier
    @ManyToOne
    @JoinColumn(name = "matiere_dominante_id")
    private Matiere matiereDominante;
    
    @ManyToOne
    @JoinColumn(name = "matiere_secondaire_id")
    private Matiere matiereSecondaire;
    
    // Constructeurs
    public Enseignant() {
        this.id = UUID.randomUUID().toString();
    }
    
    public Enseignant(String nom, String prenom, String matricule, String email, 
                     String telephone, Integer heuresMaxHebdo) {
        this();
        this.nom = nom;
        this.prenom = prenom;
        this.matricule = matricule;
        this.email = email;
        this.telephone = telephone;
        this.heuresMaxHebdo = heuresMaxHebdo;
    }
    
    // Getters and Setters
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
    
    public Set<Enseignement> getEnseignements() { return enseignements; }
    public void setEnseignements(Set<Enseignement> enseignements) { this.enseignements = enseignements; }
    
    public Set<Disponibilite> getDisponibilites() { return disponibilites; }
    public void setDisponibilites(Set<Disponibilite> disponibilites) { this.disponibilites = disponibilites; }
    
    public Integer getHeuresMaxHebdo() { return heuresMaxHebdo; }
    public void setHeuresMaxHebdo(Integer heuresMaxHebdo) { this.heuresMaxHebdo = heuresMaxHebdo; }
    
    // OPTIONNEL: Getters/Setters pour les matières
    public Matiere getMatiereDominante() { return matiereDominante; }
    public void setMatiereDominante(Matiere matiereDominante) { this.matiereDominante = matiereDominante; }
    
    public Matiere getMatiereSecondaire() { return matiereSecondaire; }
    public void setMatiereSecondaire(Matiere matiereSecondaire) { this.matiereSecondaire = matiereSecondaire; }
}