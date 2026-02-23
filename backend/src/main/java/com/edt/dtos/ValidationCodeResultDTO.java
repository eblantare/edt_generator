// C:\projets\java\edt-generator\backend\src\main\java\com\edt\dtos\ValidationCodeResultDTO.java
package com.edt.dtos;

public class ValidationCodeResultDTO {
    private boolean success;
    private String message;
    private String token;
    private String utilisateurId;
    private String email;
    private String role;
    
    public ValidationCodeResultDTO() {}
    
    public ValidationCodeResultDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public ValidationCodeResultDTO(boolean success, String message, String token, 
                                   String utilisateurId, String email, String role) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.utilisateurId = utilisateurId;
        this.email = email;
        this.role = role;
    }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(String utilisateurId) { this.utilisateurId = utilisateurId; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}