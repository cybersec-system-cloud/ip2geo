package cybersec.cloud.ip2geo;

import java.net.ConnectException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
    
    private final String defaultValue;
    private final Map<String,IPGeoInfo> ips;
    private final JSONParser parser;
    private final WebTarget ipPubblico;
    private final WebTarget geojs;
    
    public IP2Geo(String defaultValue, String port) {
        this.defaultValue = defaultValue;
        ips = new HashMap<String,IPGeoInfo>();
        
        // Crea un JSON parser (utility)
        parser = new JSONParser();
        // Collegamento ai servizi utilizzati
        Client c = ClientBuilder.newClient();
        ipPubblico = c.target("http://localhost:" + port + "/ipPubblico");
        geojs = c.target("https://get.geojs.io/v1/ip/geo");
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
    
    @GET
    @Path("/{ip}")
    public Response recuperaIP(@PathParam("ip") String ip) throws ParseException {
        // Invoca "ipPubblico" per verifica dell'IP passato
        Response rPub;
        try {
            rPub = ipPubblico.path(ip).request().get();
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
        
        // Se le informazioni sull'IP sono disponibili localmente, vengono restituite
        if(ips.containsKey(ip)) {
            return Response.ok().entity(ips.get(ip)).build();
        }
        // Se invece non ci sono, invoca GeoJS
        else {
            Response rGeo;
            try {
                rGeo = geojs.path(ip + ".json").request().get();
            } catch (ProcessingException e) {
                if(e.getCause() instanceof ConnectException) {
                    return Response.status(Status.INTERNAL_SERVER_ERROR)
                            .entity("Impossible geolocalizzare l'IP. Riprovare")
                            .type(MediaType.TEXT_PLAIN)
                            .build();
                }
                else throw e;
            }
            // Se "geojs" restituisce errore, viene inoltrato al cliente
            if(rGeo.getStatus() != Status.OK.getStatusCode()) {
                return rGeo;
            }
            // Altrimenti, costruisce un IPGeoInfo con le informazioni ottenute..
            JSONObject geoInfo = (JSONObject) parser.parse(rGeo.readEntity(String.class));
            String countryCode = defaultValue;
            if(geoInfo.containsKey("country_code"))
                    countryCode = (String) geoInfo.get("country_code");
            String country = defaultValue;
            if(geoInfo.containsKey("country"))
                    country = (String) geoInfo.get("country");
            String city = defaultValue;
            if(geoInfo.containsKey("city"))
                    city = (String) geoInfo.get("city");
            String latitude = defaultValue;
            if(geoInfo.containsKey("latitude"))
                    latitude = (String) geoInfo.get("latitude");
            String longitude = defaultValue;
            if(geoInfo.containsKey("longitude"))
                    longitude = (String) geoInfo.get("longitude");
            IPGeoInfo ipInfo = new IPGeoInfo(ip,countryCode,country,city,latitude,longitude);
            // ..e lo restituisce al cliente
            return Response.ok().entity(ipInfo).type(MediaType.APPLICATION_JSON).build();
        }
    }
    
    @PUT
    @Path("/{ip}")
    public Response aggiornaIP(@PathParam("ip") String ip, IPGeoInfo ipInfo) throws ParseException {
        // Verifica che l'IP passato e quello contenuto in "ipInfo" coincidano
        if(!ip.equals(ipInfo.getIP())) {
            return Response.status(Status.BAD_REQUEST)
                .entity("Gli IP forniti devono coincidere")
                .type(MediaType.TEXT_PLAIN)
                .build();
        }
        // Invoca "ipPubblico" per verifica dell'IP passato
        Response rPub;
        try {
            rPub = ipPubblico.path(ip).request().get();
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
        
        // Se non ci sono informazioni su IP, restituisce errore (NOT FOUND)
        if(!ips.containsKey(ip)) {
            return Response.status(Status.NOT_FOUND)
                    .entity("IP non presente nel sistema")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
        
        // Sostituisce le informazioni su IP in "ips" con le nuove...
        if(ipInfo.getCountryCode().isEmpty()) ipInfo.setCountryCode(defaultValue);
        if(ipInfo.getCountry().isEmpty()) ipInfo.setCountry(defaultValue);
        if(ipInfo.getCity().isEmpty()) ipInfo.setCity(defaultValue);
        if(ipInfo.getLatitude().isEmpty()) ipInfo.setLatitude(defaultValue);
        if(ipInfo.getLongitude().isEmpty()) ipInfo.setLongitude(defaultValue);
        ips.replace(ip,ipInfo);
        // ..e restituisce "200 OK"
        return Response.ok()
                .entity("IP aggiornato correttamente")
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
    
    @DELETE
    @Path("/{ip}")
    public Response rimuoviIP(@PathParam("ip") String ip) throws ParseException {
        // Invoca "ipPubblico" per verifica dell'IP passato
        Response rPub;
        try {
            rPub = ipPubblico.path(ip).request().get();
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
        
        // Se non ci sono informazioni su IP, restituisce errore (NOT FOUND)
        if(!ips.containsKey(ip)) {
            return Response.status(Status.NOT_FOUND)
                    .entity("IP non presente nel sistema")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
        
        // Elimina l'IP dal sistema
        ips.remove(ip);
        return Response.ok()
                .entity("IP eliminato dal sistema")
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
}
