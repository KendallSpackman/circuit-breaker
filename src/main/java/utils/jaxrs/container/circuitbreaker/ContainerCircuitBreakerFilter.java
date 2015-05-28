/////*
//// * Copyright (c) 2014 Gregor Roth
//// *
//// * Licensed under the Apache License, Version 2.0 (the "License");
//// * you may not use this file except in compliance with the License.
//// * You may obtain a copy of the License at
//// *
//// *  http://www.apache.org/licenses/LICENSE-2.0
//// *
//// * Unless required by applicable law or agreed to in writing, software
//// * distributed under the License is distributed on an "AS IS" BASIS,
//// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//// * See the License for the specific language governing permissions and
//// * limitations under the License.
//// */
package utils.jaxrs.container.circuitbreaker;
public class ContainerCircuitBreakerFilter{

}
////
////
////
////import java.io.IOException;
////
//import com.sun.jersey.spi.container.ContainerRequest;
//import com.sun.jersey.spi.container.ContainerRequestFilter;
//import com.sun.jersey.spi.container.ContainerResponse;
//import com.sun.jersey.spi.container.ContainerResponseFilter;
//
////import org.threeten.bp.Duration;
////
////import javax.ws.rs.core.Context;
////import javax.ws.rs.ext.Provider;
////
////import org.familysearch.platform.utils.circuitbreaker.CircuitBreakerRegistry;
////import org.familysearch.platform.utils.circuitbreaker.CircuitOpenedException;
////import org.familysearch.platform.utils.circuitbreaker.metrics.MetricsRegistry;
////import org.familysearch.platform.utils.circuitbreaker.metrics.Transaction;
////
////
////
////@Provider
//public class ContainerCircuitBreakerFilter implements ContainerRequestFilter, ContainerResponseFilter {
//    @Override
//    public ContainerRequest filter(ContainerRequest containerRequest) {
//        return null;
//    }
//
//    @Override
//    public ContainerResponse filter(ContainerRequest containerRequest, ContainerResponse containerResponse) {
//        return null;
//    }
////
////    private static final String TRANSACTION = "circuit_breaker_transaction";
////
////    private final CircuitBreakerRegistry circuitBreakerRegistry;
////    private final MetricsRegistry metricsRegistry;
////
////
////    @Context
////    private ResourceInfo resourceInfo;
////
////
////    public ContainerCircuitBreakerFilter() {
////        metricsRegistry = new MetricsRegistry();
////        Duration thresholdSlowTransaction = Duration.ofSeconds(5);
////        circuitBreakerRegistry = new CircuitBreakerRegistry(new OverloadBasedHealthPolicy(metricsRegistry, thresholdSlowTransaction));
////    }
////
////
////
////    @Override
////    public void filter(ContainerRequestContext requestContext) throws IOException {
////        String scope = resourceInfo.getResourceClass().getName() + "#" + resourceInfo.getResourceClass().getName();
////
////        if (!circuitBreakerRegistry.get(scope).isRequestAllowed()) {
////            throw new CircuitOpenedException("circuit is open");
////        }
////
////        Transaction transaction = metricsRegistry.transactions(scope).openTransaction();
////        requestContext.setProperty(TRANSACTION, transaction);
////    }
////
////
////
////    @Override
////    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
////        boolean isFailed = responseContext.getStatus() >= 500;
////        Transaction.close(requestContext.getProperty(TRANSACTION), isFailed);
////    }
//}
