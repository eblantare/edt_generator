// C:\projets\java\edt-generator\backend\src\main\java\com\edt\dtos\VerificationEmailDTO.java
package com.edt.dtos;

public class VerificationEmailDTO {
    private String email;
    
    public VerificationEmailDTO() {}
    
    public VerificationEmailDTO(String email) {
        this.email = email;
    }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
