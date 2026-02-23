// C:\projets\java\edt-generator\backend\src\main\java\com\edt\dtos\ValidationCodeDTO.java
package com.edt.dtos;

public class ValidationCodeDTO {
    private String utilisateurId;
    private String code;
    
    public ValidationCodeDTO() {}
    
    public ValidationCodeDTO(String utilisateurId, String code) {
        this.utilisateurId = utilisateurId;
        this.code = code;
    }
    
    public String getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(String utilisateurId) { this.utilisateurId = utilisateurId; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}