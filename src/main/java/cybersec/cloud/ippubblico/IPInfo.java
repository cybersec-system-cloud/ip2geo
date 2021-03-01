package cybersec.cloud.ippubblico;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IPInfo {
    
    private String ip;
    private String type;
    
    public IPInfo() {
        // Ci pensa Jackson
    }
    
    public IPInfo(String ip, String type) {
        this.ip = ip;
        this.type = type;
    }
    
    @JsonProperty
    public String getIP() {
        return this.ip;
    }
    
    @JsonProperty
    public String getType() {
        return this.type;
    }
    
}
