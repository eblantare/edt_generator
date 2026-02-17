package com.edt.entities;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "enseignements", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"enseignant_id", "classe_id", "matiere_id"}))
public class Enseignement {
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "enseignant_id", nullable = false)
    private Enseignant enseignant;
    
    @ManyToOne
    @JoinColumn(name = "classe_id", nullable = false)
    private Classe classe;
    
    @ManyToOne
    @JoinColumn(name = "matiere_id", nullable = false)
    private Matiere matiere;
    
    @Column(name = "heures_par_semaine")
    private Integer heuresParSemaine;
    
    // Constructeurs
    public Enseignement() {
        this.id = UUID.randomUUID().toString();
    }
    
    public Enseignement(Enseignant enseignant, Classe classe, Matiere matiere, Integer heuresParSemaine) {
        this();
        this.enseignant = enseignant;
        this.classe = classe;
        this.matiere = matiere;
        this.heuresParSemaine = heuresParSemaine;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Enseignant getEnseignant() { return enseignant; }
    public void setEnseignant(Enseignant enseignant) { this.enseignant = enseignant; }
    
    public Classe getClasse() { return classe; }
    public void setClasse(Classe classe) { this.classe = classe; }
    
    public Matiere getMatiere() { return matiere; }
    public void setMatiere(Matiere matiere) { this.matiere = matiere; }
    
    public Integer getHeuresParSemaine() { return heuresParSemaine; }
    public void setHeuresParSemaine(Integer heuresParSemaine) { 
        this.heuresParSemaine = heuresParSemaine; 
    }
}