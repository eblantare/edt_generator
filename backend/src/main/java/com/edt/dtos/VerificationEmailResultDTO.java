// C:\projets\java\edt-generator\backend\src\main\java\com\edt\dtos\VerificationEmailResultDTO.java
package com.edt.dtos;

public class VerificationEmailResultDTO {
    private boolean success;
    private String message;
    private boolean existe;
    
    public VerificationEmailResultDTO() {}
    
    public VerificationEmailResultDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public VerificationEmailResultDTO(boolean success, String message, boolean existe) {
        this.success = success;
        this.message = message;
        this.existe = existe;
    }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public boolean isExiste() { return existe; }
    public void setExiste(boolean existe) { this.existe = existe; }
}
