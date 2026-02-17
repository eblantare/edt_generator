// C:\projets\java\edt-generator\backend\src\main\java\com\edt\dtos\GenerationRequestDTO.java
package com.edt.dtos;

public class GenerationRequestDTO {
    private String anneeScolaire;
    private String classeId;
    private String enseignantId;
    private String type; // "global", "classe", "enseignant"
    private GenerationOptionsDTO options;
    
    // Getters et Setters
    public String getAnneeScolaire() { return anneeScolaire; }
    public void setAnneeScolaire(String anneeScolaire) { this.anneeScolaire = anneeScolaire; }
    
    public String getClasseId() { return classeId; }
    public void setClasseId(String classeId) { this.classeId = classeId; }
    
    public String getEnseignantId() { return enseignantId; }
    public void setEnseignantId(String enseignantId) { this.enseignantId = enseignantId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public GenerationOptionsDTO getOptions() { return options; }
    public void setOptions(GenerationOptionsDTO options) { this.options = options; }
}