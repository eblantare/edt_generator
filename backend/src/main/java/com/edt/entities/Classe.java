package com.edt.entities;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "classes")
public class Classe {
    @Id
    private String id;
    
    private String nom;
    private String niveau; // 6ème, 5ème, 2nde, Terminale, etc.
    private String filiere; // Générale, Technologique, Professionnelle
    private Integer effectif;
    
    @OneToMany(mappedBy = "classe", cascade = CascadeType.ALL)
    private Set<EmploiDuTemps> emploisDuTemps = new HashSet<>();
    
    @OneToMany(mappedBy = "classe")
    private Set<Enseignement> enseignements = new HashSet<>();
    
    @OneToMany(mappedBy = "classe")
    private Set<VolumeHoraire> volumesHoraires = new HashSet<>();
    
    // Constructeurs
    public Classe() {
        this.id = UUID.randomUUID().toString();
    }
    
    public Classe(String nom, String niveau, String filiere, Integer effectif) {
        this();
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
    
    public Set<EmploiDuTemps> getEmploisDuTemps() { return emploisDuTemps; }
    public void setEmploisDuTemps(Set<EmploiDuTemps> emploisDuTemps) { this.emploisDuTemps = emploisDuTemps; }
    
    public Set<Enseignement> getEnseignements() { return enseignements; }
    public void setEnseignements(Set<Enseignement> enseignements) { this.enseignements = enseignements; }
    
    public Set<VolumeHoraire> getVolumesHoraires() { return volumesHoraires; }
    public void setVolumesHoraires(Set<VolumeHoraire> volumesHoraires) { this.volumesHoraires = volumesHoraires; }
}