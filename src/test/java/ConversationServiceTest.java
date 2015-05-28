import client.ConversationService;
import org.testng.annotations.Test;
import utils.jaxrs.client.circuitbreaker.ClientCircuitBreakerFilter;

import java.lang.Exception;import java.lang.InterruptedException;import java.lang.System;import java.lang.Thread;import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import static org.testng.Assert.assertEquals;

@Test(groups="All")
public class ConversationServiceTest {
    public void testConversation() throws InterruptedException {
        ConversationService conversationService = new ConversationService();
        for(int i = 0; i < 120; i++) {
            try {
                System.out.println(conversationService.getGreeting());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                //e.printStackTrace();
            }
        }
        System.out.println("\n\n");

        Thread.sleep(3500);
        try {
            System.out.println(conversationService.getGreeting());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
        }

    }

    public void testParseUri() throws URISyntaxException {
        ClientCircuitBreakerFilter clientCircuitBreakerFilter = new ClientCircuitBreakerFilter();
        ArrayList<URI> uri = new ArrayList<>();
        uri.add(new URI("http://integration.familysearch.org/oss/person/1234-567"));
        uri.add(new URI("http://integration.familysearch.org/oss/person/1234-567?sessionId=USYsdafj"));
        uri.add(new URI("http://integration.familysearch.org/oss/person/1234-567#sessionId=USYsdafj"));
        uri.add(new URI("http://integration.familysearch.org/oss/person/1234-567?sessionId=USYsdafj#sessionId=USYsdafj"));
        uri.add(new URI("http://integration.familysearch.org/oss?sessionId=USYsdafj"));
        uri.add(new URI("http://integration.familysearch.org/oss#sessionId=USYsdafj"));
        uri.add(new URI("http://integration.familysearch.org/oss?sessionId=something#sessionId=USYsdafj"));
        for (URI u : uri) {
            assertEquals(clientCircuitBreakerFilter.getFirstInUriPath(u), "oss");
        }

        ArrayList<URI> emptyPathUri = new ArrayList<>();
        emptyPathUri.add(new URI("http://integration.familysearch.org/"));
        emptyPathUri.add(new URI("http://integration.familysearch.org"));
        emptyPathUri.add(new URI("http://integration.familysearch.org?sessionId=USYsdafj#sessionId=USYsdafj"));
        for(URI u : emptyPathUri){
            assertEquals(clientCircuitBreakerFilter.getFirstInUriPath(u), "");
        }
    }
}
