import client.ConversationService;
import org.testng.annotations.Test;
import utils.jaxrs.client.circuitbreaker.ClientCircuitBreakerFilter;

import javax.ws.rs.core.UriBuilder;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.Exception;import java.lang.InterruptedException;import java.lang.System;import java.lang.Thread;import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import static org.testng.Assert.assertEquals;

@Test(groups="All")
public class ConversationServiceTest {
    public class ThreadClass implements Runnable {
        public void run() {
            ConversationService conversationService = new ConversationService(UriBuilder.fromUri("http://localhost:8080/hello-world-500").build());
            ConversationService conversationService2 = new ConversationService(UriBuilder.fromUri("http://localhost:8080/hello-world-500?name=500").build());
            for (int i = 0; i < 10; i++) {
                try {
                    System.out.println(conversationService.getGreeting());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
            for (int i = 0; i < 15; i++) {
                try {
                    System.out.println(conversationService2.getGreeting());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
            while (true) {
                try {
                    System.out.println(conversationService.getGreeting());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public void testConversationThreaded(){
        ArrayList<Thread> threadPool = new ArrayList<>();
        for(int i = 0; i < 10; ++i) {
            threadPool.add(new Thread(new ThreadClass()));
        }
        for(Thread t : threadPool) {
            t.start();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
        for(Thread t : threadPool) {
            try {
                t.join();
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
    }

    public void testConversation() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("withMetricsFilter.txt", "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ConversationService conversationService = new ConversationService();
        for(int ctr = 0; ctr < 15; ctr++) {
            long begin = System.nanoTime();
            for (int i = 0; i < 120; i++) {
                try {
                    conversationService.getGreeting();
                } catch (Exception e) {
                    //System.out.println(e.getMessage());
                    //e.printStackTrace();
                }
            }
            long end = System.nanoTime();
            writer.println(Long.toString((end - begin) / 1000000));
        }
        writer.close();
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
