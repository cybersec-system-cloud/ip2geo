package cybersec.cloud.ip2geo;

import java.net.ConnectException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@Path("/ip2geo")
@Produces(MediaType.APPLICATION_JSON)
public class IP2Geo {
    
    private String defaultValue;
    private Map<String,IPGeoInfo> ips;
    private JSONParser parser;
    private WebTarget ipPubblico;
    
    public IP2Geo(String defaultValue) {
        this.defaultValue = defaultValue;
        ips = new HashMap<String,IPGeoInfo>();
        
        // Crea un JSON parser (utility)
        parser = new JSONParser();
        // Collegamento ai servizi utilizzati
        Client c = ClientBuilder.newClient();
        ipPubblico = c.target("http://localhost:8080/IPPubblico");
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response aggiungiIP(IPGeoInfo ipInfo) throws ParseException {
        // Invoca "ipPubblico" per verifica dell'IP passato
        Response rPub;
        try {
            rPub = ipPubblico.path(ipInfo.getIP()).request().get();
        } catch(ProcessingException e) {
            if(e.getCause() instanceof ConnectException) {
                return Response.status(Status.INTERNAL_SERVER_ERROR)
                        .entity("Impossibile validare l'IP passato. Riprovare")
                        .type(MediaType.TEXT_PLAIN)
                        .build();
            }
            else throw e;
        }
        // Se "ipPubblico" restituisce errore, allora l'IP non è valido
        if(rPub.getStatus() != Status.OK.getStatusCode()) {
            return rPub;
        }
        
        // Se l'IP è privato, restituisce errore (BAD REQUEST)
        JSONObject body = (JSONObject) parser.parse(rPub.readEntity(String.class));
        String tipoIP = (String) body.get("type");
        if(tipoIP.equals("private")) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("L'indirizzo IP deve essere pubblico")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
        
        // Se l'IP è già presente, restituisce errore (CONFLICT)
        if(ips.containsKey(ipInfo.getIP())) {
            return Response.status(Status.CONFLICT)
                    .entity("L'indirizzo IP è già presente nel sistema")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
        
        // Inserisce le nuove informazioni su IP in "ips"...
        if(ipInfo.getCountryCode().isEmpty()) ipInfo.setCountryCode(defaultValue);
        if(ipInfo.getCountry().isEmpty()) ipInfo.setCountry(defaultValue);
        if(ipInfo.getCity().isEmpty()) ipInfo.setCity(defaultValue);
        if(ipInfo.getLatitude().isEmpty()) ipInfo.setLatitude(defaultValue);
        if(ipInfo.getLongitude().isEmpty()) ipInfo.setLongitude(defaultValue);
        ips.put(ipInfo.getIP(),ipInfo);
        // ..e restituisce "201 Created"
        URI u = UriBuilder.fromResource(IP2Geo.class).path(ipInfo.getIP()).build();
        return Response.created(u).build();
    }
    
}
