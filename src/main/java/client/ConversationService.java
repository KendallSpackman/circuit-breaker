package client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.familysearch.jersey.JerseyFacadeClient;
import utils.jaxrs.client.circuitbreaker.ClientCircuitBreakerFilter;
import utils.metrics.MetricsFilter;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class ConversationService {

    private static Client client2;
    private URI uri;
    //static initializer -- when loaded
    static{
        client2 = new JerseyFacadeClient();
        client2.addFilter(new MetricsFilter());
        client2.addFilter(new ClientCircuitBreakerFilter());
        client2.setReadTimeout(1000);
        client2.setConnectTimeout(10000);
//        client2.getProperties().put(ClientProperties.CONNECT_TIMEOUT, 10000);
//        client2.getProperties().put(ClientProperties.READ_TIMEOUT, 2000);
    }

    public ConversationService(){
        this(UriBuilder.fromUri("http://localhost:8080/hello-world").build());
    }

    public ConversationService(URI uri){
        this.uri = uri;
    }

    public String getGreeting(){
        //UriBuilder.
        WebResource resource = client2.resource(uri);
        ClientResponse clientResponse = resource.get(ClientResponse.class);
        if(clientResponse.getStatus() != Response.Status.OK.getStatusCode())
            System.out.println("Error: Couldn't get greeting.");
        return clientResponse.getEntity(String.class);
        //Jersey 2
//        WebTarget target = client2.target(url);
//        Invocation.Builder invocationBuilder =
//                target.request(MediaType.APPLICATION_JSON);
//        invocationBuilder.header("some-header", "true");
//        Response response = invocationBuilder.get(Response.class);
//        if(response.getStatus() != Response.Status.OK.getStatusCode())
//            System.out.println("Error: Couldn't get greeting.");
//        return response.readEntity(String.class);
    }

    public static void main(String[] args) {
        ConversationService conversationService = new ConversationService();
        conversationService.getGreeting();
    }

//    public String getAsyncGreeting() throws ExecutionException, InterruptedException {
//        WebTarget target = client2.target(url);
//        javax.ws.rs.client.AsyncInvoker asyncInvoker = target
//                .request().async();
//        final Future<Response> responseFuture = asyncInvoker.get();
//        System.out.println("Request is being processed asynchronously.");
//        final Response response = responseFuture.get();
//        if(response.getStatus() != Response.Status.OK.getStatusCode())
//            System.out.println("Error: Couldn't get greeting.");
//        return response.readEntity(String.class);
//    }
}
