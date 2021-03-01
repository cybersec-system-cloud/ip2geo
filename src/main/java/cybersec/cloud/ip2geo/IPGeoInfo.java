package cybersec.cloud.ip2geo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IPGeoInfo {
    private String ip;
    private String countryCode;
    private String country;
    private String city;
    private String latitude;
    private String longitude;
    
    public IPGeoInfo() {
        // Ci pensa Jackson
    }
    
    public IPGeoInfo(String ip, String countryCode, String country,
            String city, String latitude, String longitude) {
        this.ip = ip;
        this.countryCode = countryCode;
        this.country = country;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    @JsonProperty
    public String getIP() {
        return this.ip;
    }
    
    @JsonProperty
    public String getCountryCode() {
        return this.countryCode;
    }
    
    @JsonProperty
    public String getCountry() {
        return this.country;
    }
    
    @JsonProperty
    public String getCity() {
        return this.city;
    }
    
    @JsonProperty
    public String getLatitude() {
        return this.latitude;
    }
    
    @JsonProperty
    public String getLongitude() {
        return this.longitude;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
    
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
