package com.edt.entities;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "creneaux_horaires")
public class CreneauHoraire {
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "emploi_du_temps_id", nullable = false)
    private EmploiDuTemps emploiDuTemps;
    
    @Column(name = "jour_semaine", nullable = false)
    private String jourSemaine; // "LUNDI", "MARDI", etc.
    
    @Column(name = "heure_debut", nullable = false)
    private String heureDebut; // "07:00"
    
    @Column(name = "heure_fin", nullable = false)
    private String heureFin; // "07:55"
    
    @Column(name = "numero_creneau")
    private Integer numeroCreneau; // 1, 2, 3...
    
    @ManyToOne
    @JoinColumn(name = "enseignant_id")
    private Enseignant enseignant;
    
    @ManyToOne
    @JoinColumn(name = "classe_id")
    private Classe classe;
    
    @ManyToOne
    @JoinColumn(name = "matiere_id")
    private Matiere matiere;
    
    @Column(name = "salle")
    private String salle;
    
    @Column(name = "est_libre")
    private Boolean estLibre = false;
    
    // Constructeurs
    public CreneauHoraire() {
        this.id = UUID.randomUUID().toString();
    }
    
    public CreneauHoraire(EmploiDuTemps emploiDuTemps, String jourSemaine, 
                         String heureDebut, String heureFin, Integer numeroCreneau) {
        this();
        this.emploiDuTemps = emploiDuTemps;
        this.jourSemaine = jourSemaine;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.numeroCreneau = numeroCreneau;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public EmploiDuTemps getEmploiDuTemps() { return emploiDuTemps; }
    public void setEmploiDuTemps(EmploiDuTemps emploiDuTemps) { this.emploiDuTemps = emploiDuTemps; }
    
    public String getJourSemaine() { return jourSemaine; }
    public void setJourSemaine(String jourSemaine) { this.jourSemaine = jourSemaine; }
    
    public String getHeureDebut() { return heureDebut; }
    public void setHeureDebut(String heureDebut) { this.heureDebut = heureDebut; }
    
    public String getHeureFin() { return heureFin; }
    public void setHeureFin(String heureFin) { this.heureFin = heureFin; }
    
    public Integer getNumeroCreneau() { return numeroCreneau; }
    public void setNumeroCreneau(Integer numeroCreneau) { this.numeroCreneau = numeroCreneau; }
    
    public Enseignant getEnseignant() { return enseignant; }
    public void setEnseignant(Enseignant enseignant) { this.enseignant = enseignant; }
    
    public Classe getClasse() { return classe; }
    public void setClasse(Classe classe) { this.classe = classe; }
    
    public Matiere getMatiere() { return matiere; }
    public void setMatiere(Matiere matiere) { this.matiere = matiere; }
    
    public String getSalle() { return salle; }
    public void setSalle(String salle) { this.salle = salle; }
    
    public Boolean getEstLibre() { return estLibre; }
    public void setEstLibre(Boolean estLibre) { this.estLibre = estLibre; }
    
    // Méthode utilitaire pour vérifier si le créneau est libre (compatibilité)
    public boolean isEstLibre() {
        return estLibre != null && estLibre;
    }
}