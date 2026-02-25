// C:\projets\java\edt-generator\backend\src\main\java\com\edt\dtos\InscriptionRequestDTO.java
package com.edt.dtos;

public class InscriptionRequestDTO {
    private String email;
    private String role;

    public InscriptionRequestDTO() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}