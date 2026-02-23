// C:\projets\java\edt-generator\backend\src\main\java\com\edt\dtos\DemandeConnexionDTO.java
package com.edt.dtos;

public class DemandeConnexionDTO {
    private String email;
    
    public DemandeConnexionDTO() {}
    
    public DemandeConnexionDTO(String email) {
        this.email = email;
    }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}