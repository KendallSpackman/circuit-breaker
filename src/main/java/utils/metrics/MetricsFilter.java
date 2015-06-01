package utils.metrics;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

import java.net.URI;


public class MetricsFilter extends ClientFilter {
    static final MetricRegistry metrics = new MetricRegistry();
    final JmxReporter reporter;


    public MetricsFilter(){
        reporter = JmxReporter.forRegistry(metrics).build();
        reporter.start();
    }


    @Override
    public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {
        String scope = clientRequest.getURI().getHost() + "/" + getFirstInUriPath(clientRequest.getURI());

        ClientResponse clientResponse;
        final Timer responses = metrics.timer(MetricRegistry.name(MetricsFilter.class, "responses", scope));
        final Timer.Context context = responses.time();
        try {
            clientResponse = getNext().handle(clientRequest);
            return clientResponse;
        } finally{
            context.stop();
        }
    }


    public String getFirstInUriPath(URI uri){
        String path = uri.getPath();
        int indexOfSlash = path.indexOf('/', 1);
        if(indexOfSlash > 1) {
            return path.substring(1, indexOfSlash);
        }
        else if(path.length() > 0) {
            return path.substring(1);
        }
        else return "";
    }
}