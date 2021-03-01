package cybersec.cloud.ip2geo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class IP2GeoConfiguration extends Configuration {
    
    private String defaultValue;
    
    @JsonProperty
    public String getDefaultValue() {
        return defaultValue;
    }
    
    @JsonProperty
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
