package cybersec.cloud.server;

import cybersec.cloud.ippubblico.IPPubblico;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;

public class Server extends Application<Configuration> {
    
    public static void main(String[] args) throws Exception {
        new Server().run(args);
    }

    @Override
    public void run(Configuration t, Environment e) throws Exception {
        // Registrazione del servizio IPPubblico
        final IPPubblico ipPubblico = new IPPubblico();
        e.jersey().register(ipPubblico);
    }
    
}
