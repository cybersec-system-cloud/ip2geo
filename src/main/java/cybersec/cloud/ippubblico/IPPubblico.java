package cybersec.cloud.ippubblico;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.StringUtils;

@Path("/ipPubblico")
@Produces(MediaType.APPLICATION_JSON)
public class IPPubblico {
    
    public IPPubblico() {};

    @GET
    @Path("/{ip}")
    public Response checkIP(@PathParam("ip") String ip) {
        // Scompone "ip" in parti separate da "."
        String[] parti = ip.split("\\.");
        
        // Controlla che sia composto da 4 numeri interi separati da 3 punti
        if (parti.length != 4) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("L'IP passato non è formato da 4 numeri")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
        // Controlla che i 4 numeri siano compresi tra 0 e 255
        for(int i=0; i<parti.length; i++) {
            if(!StringUtils.isNumeric(parti[i])) {
                return Response.status(Status.BAD_REQUEST)
                        .entity("L'IP passato non è formato da 4 numeri")
                        .type(MediaType.TEXT_PLAIN)
                        .build();
            }
            int n = Integer.parseInt(parti[i]);
            if(n < 0 || n > 255) {
                return Response.status(Status.BAD_REQUEST)
                        .entity("L'IP passato non è formato da 4 numeri compresi tra 0 e 255")
                        .type(MediaType.TEXT_PLAIN)
                        .build();
            }
        }
        
        // Isola i numeri che compongono l'IP
        int n1 = Integer.parseInt(parti[0]);
        int n2 = Integer.parseInt(parti[1]);
        int n3 = Integer.parseInt(parti[2]);
        int n4 = Integer.parseInt(parti[3]);
        
        // Controlla che l'IP sia privato
        if (n1 == 10) {
            return Response.ok()
                    .entity(new IPInfo(ip,"private"))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        if (n1 == 172) {
            if (n2 <= 31 && n2 >= 16) {
                return Response.ok()
                        .entity(new IPInfo(ip,"private"))
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
        }
        if (n1 == 192 && n2 == 168) {
            return Response.ok()
                    .entity(new IPInfo(ip,"private"))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        
        // Se arriva qui, l'IP è pubblico
        return Response.ok()
                .entity(new IPInfo(ip,"public"))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
