package com.example.ubl.model;

/** Seller party information. */
public class Seller {
    private String name;
    private String vatNumber;
    private String crn;
    private String street;
    private String city;
    private String region;
    private String postalCode;
    private String countryCode = "SA";

    public Seller() {}

    public Seller(String name, String vatNumber, String crn,
                  String street, String city, String region,
                  String postalCode) {
        this.name = name;
        this.vatNumber = vatNumber;
        this.crn = crn;
        this.street = street;
        this.city = city;
        this.region = region;
        this.postalCode = postalCode;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVatNumber() { return vatNumber; }
    public void setVatNumber(String vatNumber) { this.vatNumber = vatNumber; }
    public String getCrn() { return crn; }
    public void setCrn(String crn) { this.crn = crn; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
}
