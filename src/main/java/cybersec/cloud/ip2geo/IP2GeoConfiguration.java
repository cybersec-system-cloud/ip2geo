package cybersec.cloud.ip2geo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class IP2GeoConfiguration extends Configuration {
    
    private String defaultValue;
    private String port;
    
    @JsonProperty
    public String getDefaultValue() {
        return defaultValue;
    }
    
    @JsonProperty
    public String getPort() {
        return port;
    }
    
    @JsonProperty
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    } 
    @JsonProperty
    public void setPort(String port) {
        this.port = port;
    }
}
