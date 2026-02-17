package com.edt.entities;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "disponibilites")
public class Disponibilite {
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "enseignant_id")
    private Enseignant enseignant;
    
    private String jour; // "LUNDI", "MARDI", etc. ou "Lundi", "Mardi"
    
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Boolean disponible;
    
    // Constructeurs
    public Disponibilite() {
        this.id = UUID.randomUUID().toString();
        this.disponible = true;
    }
    
    public Disponibilite(Enseignant enseignant, String jour, LocalTime heureDebut, LocalTime heureFin, Boolean disponible) {
        this();
        this.enseignant = enseignant;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.disponible = disponible;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Enseignant getEnseignant() { return enseignant; }
    public void setEnseignant(Enseignant enseignant) { this.enseignant = enseignant; }
    
    public String getJour() { return jour; }
    public void setJour(String jour) { this.jour = jour; }
    
    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }
    
    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }
    
    public Boolean getDisponible() { return disponible; }
    public Boolean isDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }
}