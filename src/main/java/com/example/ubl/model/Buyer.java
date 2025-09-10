package com.example.ubl.model;

/** Buyer party information. */
public class Buyer {
    private String name;
    private String vatNumber;
    private String street;
    private String city;
    private String region;
    private String postalCode;
    private String countryCode;

    public Buyer() {}

    public Buyer(String name, String vatNumber,
                 String street, String city,
                 String region, String postalCode,
                 String countryCode) {
        this.name = name;
        this.vatNumber = vatNumber;
        this.street = street;
        this.city = city;
        this.region = region;
        this.postalCode = postalCode;
        this.countryCode = countryCode;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVatNumber() { return vatNumber; }
    public void setVatNumber(String vatNumber) { this.vatNumber = vatNumber; }
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
