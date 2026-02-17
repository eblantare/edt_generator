package com.edt.dtos;

public class GenerationOptionsDTO {
    private boolean verifierConflits = true;
    private boolean optimiserRepartition = true;
    private boolean genererSalles = false;
    private boolean respecterContraintesEPS = true;
    private boolean placerPauses = true;
    
    // Constructeur par défaut
    public GenerationOptionsDTO() {}
    
    // Getters
    public boolean isVerifierConflits() {
        return verifierConflits;
    }
    
    public boolean isOptimiserRepartition() {
        return optimiserRepartition;
    }
    
    public boolean isGenererSalles() {
        return genererSalles;
    }
    
    public boolean isRespecterContraintesEPS() {
        return respecterContraintesEPS;
    }
    
    public boolean isPlacerPauses() {
        return placerPauses;
    }
    
    // Setters
    public void setVerifierConflits(boolean verifierConflits) {
        this.verifierConflits = verifierConflits;
    }
    
    public void setOptimiserRepartition(boolean optimiserRepartition) {
        this.optimiserRepartition = optimiserRepartition;
    }
    
    public void setGenererSalles(boolean genererSalles) {
        this.genererSalles = genererSalles;
    }
    
    public void setRespecterContraintesEPS(boolean respecterContraintesEPS) {
        this.respecterContraintesEPS = respecterContraintesEPS;
    }
    
    public void setPlacerPauses(boolean placerPauses) {
        this.placerPauses = placerPauses;
    }
}