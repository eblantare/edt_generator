package com.edt.entities;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "volumes_horaires")
public class VolumeHoraire {
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "classe_id")
    private Classe classe;
    
    @ManyToOne
    @JoinColumn(name = "matiere_id")
    private Matiere matiere;
    
    private Integer heuresHebdomadaires;
    
    // Constructeurs
    public VolumeHoraire() {
        this.id = UUID.randomUUID().toString();
    }
    
    public VolumeHoraire(Classe classe, Matiere matiere, Integer heuresHebdomadaires) {
        this();
        this.classe = classe;
        this.matiere = matiere;
        this.heuresHebdomadaires = heuresHebdomadaires;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Classe getClasse() { return classe; }
    public void setClasse(Classe classe) { this.classe = classe; }
    
    public Matiere getMatiere() { return matiere; }
    public void setMatiere(Matiere matiere) { this.matiere = matiere; }
    
    public Integer getHeuresHebdomadaires() { return heuresHebdomadaires; }
    public void setHeuresHebdomadaires(Integer heuresHebdomadaires) { this.heuresHebdomadaires = heuresHebdomadaires; }
}