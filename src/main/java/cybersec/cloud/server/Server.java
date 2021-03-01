package cybersec.cloud.server;

import cybersec.cloud.ip2geo.IP2Geo;
import cybersec.cloud.ip2geo.IP2GeoConfiguration;
import cybersec.cloud.ippubblico.IPPubblico;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;

public class Server extends Application<IP2GeoConfiguration> {
    
    public static void main(String[] args) throws Exception {
        new Server().run(args);
    }

    @Override
    public void run(IP2GeoConfiguration t, Environment e) throws Exception {
        // Registrazione del servizio IPPubblico
        final IPPubblico ipPubblico = new IPPubblico();
        e.jersey().register(ipPubblico);
        
        // Registrazione del servizio IP2Geo
        final IP2Geo ip2geo = new IP2Geo(t.getDefaultValue());
        e.jersey().register(ip2geo);
    }
    
}
