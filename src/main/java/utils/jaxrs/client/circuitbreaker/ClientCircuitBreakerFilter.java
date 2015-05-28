/*
 * Copyright (c) 2014 Gregor Roth
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package utils.jaxrs.client.circuitbreaker;


import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.filter.ClientFilter;
import utils.circuitbreaker.CircuitBreakerRegistry;
import utils.circuitbreaker.CircuitOpenedException;
import utils.circuitbreaker.metrics.MetricsRegistry;
import utils.circuitbreaker.metrics.Transaction;

import java.net.URI;



public class ClientCircuitBreakerFilter extends ClientFilter {

    private static final String TRANSACTION = "transaction";
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final MetricsRegistry metricsRegistry;

    public ClientCircuitBreakerFilter() {
        metricsRegistry = new MetricsRegistry();
        circuitBreakerRegistry = new CircuitBreakerRegistry(new ErrorRateBasedHealthPolicy(metricsRegistry));
    }

    @Override
    public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {
        String scope = clientRequest.getURI().getHost() + "/" + getFirstInUriPath(clientRequest.getURI());

        if (!circuitBreakerRegistry.get(scope).isRequestAllowed()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            throw new CircuitOpenedException("circuit is open");
        }

        Transaction transaction = metricsRegistry.transactions(scope).openTransaction();
        clientRequest.getProperties().put(TRANSACTION, transaction);
        ClientResponse clientResponse = null;
        try {
            clientResponse = getNextInternal().handle(clientRequest);
        } catch (ClientHandlerException | UniformInterfaceException e) {
            boolean isFailed = true;
            Transaction.close(clientRequest.getProperties().get(TRANSACTION), isFailed);
            throw e;
        }

        boolean isFailed = (clientResponse.getStatus() >= 500);
        Transaction.close(clientRequest.getProperties().get(TRANSACTION), isFailed);
        return clientResponse;
    }

    protected ClientHandler getNextInternal() {
        return getNext();
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
