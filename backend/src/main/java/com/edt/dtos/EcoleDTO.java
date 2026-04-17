package com.edt.dtos;

public class EcoleDTO {
    private String id;
    private String nom;
    private String telephone;
    private String adresse;
    private String logo;
    private String devise;
    private String dre;
    private String iesg;
    private String bp;

    public EcoleDTO() {}

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public String getDre() { return dre; }
    public void setDre(String dre) { this.dre = dre; }

    public String getIesg() { return iesg; }
    public void setIesg(String iesg) { this.iesg = iesg; }

    public String getBp() { return bp; }
    public void setBp(String bp) { this.bp = bp; }
}
