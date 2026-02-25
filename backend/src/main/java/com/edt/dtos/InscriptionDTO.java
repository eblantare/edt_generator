// C:\projets\java\edt-generator\backend\src\main\java\com\edt\dtos\InscriptionDTO.java
package com.edt.dtos;

public class InscriptionDTO {
    private String email;
    private String role;
    private String nom;
    private String prenom;
    
    public InscriptionDTO() {}
    
    public InscriptionDTO(String email, String role) {
        this.email = email;
        this.role = role;
    }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
}
