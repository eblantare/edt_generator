package com.edt.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "horaires_journaliers")
public class HoraireJournalier {
    @Id
    private String id;
    
    @Column(name = "jour_semaine", nullable = false)
    private String jourSemaine; // "LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI", "SAMEDI"
    
    @Column(name = "est_jour_cours", nullable = false)
    private Boolean estJourCours = true;
    
    @Column(name = "nombre_creneaux")
    private Integer nombreCreneaux;
    
    @Column(name = "heure_debut_matin")
    private String heureDebutMatin; // "07:00"
    
    @Column(name = "heure_fin_matin")
    private String heureFinMatin; // "12:00"
    
    @Column(name = "heure_debut_apres_midi")
    private String heureDebutApresMidi; // "15:00"
    
    @Column(name = "heure_fin_apres_midi")
    private String heureFinApresMidi; // "17:00"
    
    @Column(name = "duree_creneau_minutes")
    private Integer dureeCreneauMinutes = 55;
    
    @Column(name = "duree_recreation_minutes")
    private Integer dureeRecreationMinutes = 25;
    
    @Column(name = "duree_pause_midi_minutes")
    private Integer dureePauseMidiMinutes = 180;
    
    @ElementCollection
    @CollectionTable(name = "horaire_creneaux", joinColumns = @JoinColumn(name = "horaire_id"))
    @Column(name = "heure_debut")
    private List<String> creneaux = new ArrayList<>();
    
    // Constructeurs
    public HoraireJournalier() {
        this.id = UUID.randomUUID().toString();
    }
    
    public HoraireJournalier(String jourSemaine, Boolean estJourCours) {
        this();
        this.jourSemaine = jourSemaine;
        this.estJourCours = estJourCours;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getJourSemaine() { return jourSemaine; }
    public void setJourSemaine(String jourSemaine) { this.jourSemaine = jourSemaine; }
    
    public Boolean getEstJourCours() { return estJourCours; }
    public void setEstJourCours(Boolean estJourCours) { this.estJourCours = estJourCours; }
    
    public Integer getNombreCreneaux() { return nombreCreneaux; }
    public void setNombreCreneaux(Integer nombreCreneaux) { this.nombreCreneaux = nombreCreneaux; }
    
    public String getHeureDebutMatin() { return heureDebutMatin; }
    public void setHeureDebutMatin(String heureDebutMatin) { this.heureDebutMatin = heureDebutMatin; }
    
    public String getHeureFinMatin() { return heureFinMatin; }
    public void setHeureFinMatin(String heureFinMatin) { this.heureFinMatin = heureFinMatin; }
    
    public String getHeureDebutApresMidi() { return heureDebutApresMidi; }
    public void setHeureDebutApresMidi(String heureDebutApresMidi) { this.heureDebutApresMidi = heureDebutApresMidi; }
    
    public String getHeureFinApresMidi() { return heureFinApresMidi; }
    public void setHeureFinApresMidi(String heureFinApresMidi) { this.heureFinApresMidi = heureFinApresMidi; }
    
    public Integer getDureeCreneauMinutes() { return dureeCreneauMinutes; }
    public void setDureeCreneauMinutes(Integer dureeCreneauMinutes) { this.dureeCreneauMinutes = dureeCreneauMinutes; }
    
    public Integer getDureeRecreationMinutes() { return dureeRecreationMinutes; }
    public void setDureeRecreationMinutes(Integer dureeRecreationMinutes) { this.dureeRecreationMinutes = dureeRecreationMinutes; }
    
    public Integer getDureePauseMidiMinutes() { return dureePauseMidiMinutes; }
    public void setDureePauseMidiMinutes(Integer dureePauseMidiMinutes) { this.dureePauseMidiMinutes = dureePauseMidiMinutes; }
    
    public List<String> getCreneaux() { return creneaux; }
    public void setCreneaux(List<String> creneaux) { this.creneaux = creneaux; }
}