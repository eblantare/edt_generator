// C:\projets\java\edt-generator\backend\src\main\java\com\edt\dtos\DemandeConnexionResultDTO.java
package com.edt.dtos;

public class DemandeConnexionResultDTO {
    private boolean success;
    private String message;
    private String utilisateurId;
    
    public DemandeConnexionResultDTO() {}
    
    public DemandeConnexionResultDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public DemandeConnexionResultDTO(boolean success, String message, String utilisateurId) {
        this.success = success;
        this.message = message;
        this.utilisateurId = utilisateurId;
    }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(String utilisateurId) { this.utilisateurId = utilisateurId; }
}