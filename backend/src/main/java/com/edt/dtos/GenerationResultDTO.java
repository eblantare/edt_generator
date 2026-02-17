// C:\projets\java\edt-generator\backend\src\main\java\com\edt\dtos\GenerationResultDTO.java
package com.edt.dtos;

public class GenerationResultDTO {
    private boolean success;
    private String message;
    private String emploiDuTempsId;
    private Object details;
    
    public GenerationResultDTO() {}
    
    public GenerationResultDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public GenerationResultDTO(boolean success, String message, String emploiDuTempsId) {
        this.success = success;
        this.message = message;
        this.emploiDuTempsId = emploiDuTempsId;
    }
    
    // Getters et Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getEmploiDuTempsId() { return emploiDuTempsId; }
    public void setEmploiDuTempsId(String emploiDuTempsId) { this.emploiDuTempsId = emploiDuTempsId; }
    
    public Object getDetails() { return details; }
    public void setDetails(Object details) { this.details = details; }
}