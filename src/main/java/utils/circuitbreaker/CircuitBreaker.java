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
package utils.circuitbreaker;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.familysearch.logging.api.LogFactory;
import org.familysearch.logging.api.Logger;
import org.joda.time.Duration;
import org.joda.time.Instant;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;


public class CircuitBreaker {
    private final String scope;
    private final Random random = new Random(); 
    private final Duration openStateTimeout;
    private final HealthPolicy policy;
    private static final Logger LOGGER = LogFactory.getLogger("aspect.system-performance.logger");

    private final AtomicReference<CircuitBreakerState> state = new AtomicReference<CircuitBreakerState>(new ClosedState());


    
    CircuitBreaker(String scope, HealthPolicy healthPolicy, Duration openStateTimeout) {
        this.scope = scope;
        this.policy = new CachedCircuitBreakerPolicy(healthPolicy, new Duration(3000));
        this.openStateTimeout = openStateTimeout;
    }
    
    
    public boolean isRequestAllowed() {
        return state.get().isRequestAllowed();
    }
    

    
    private final class ClosedState implements CircuitBreakerState {
        
        @Override
        public boolean isRequestAllowed() {
            if(policy.isHealthy(scope)) return true;
            else{
                LOGGER.info("Circuit breaker state change state=open host=" + scope);
                return changeState(new OpenState()).isRequestAllowed();
            }
            // return (policy.isHealthy(scope)) ? true
            //                                 : changeState(new OpenState()).isRequestAllowed();
        }
    }
    
    
     
    private final class OpenState implements CircuitBreakerState {
        private final Instant exitDate = Instant.now().plus(openStateTimeout);
        
        @Override
        public boolean isRequestAllowed() {
            if(!Instant.now().isAfter(exitDate)) return false;
            else{
                LOGGER.info("Circuit breaker state change state=half-open host=" + scope);
                return changeState(new HalfOpenState()).isRequestAllowed();
            }
//            return (Instant.now().isAfter(exitDate)) ? changeState(new HalfOpenState()).isRequestAllowed()
//                                                     : false;
        }
    }

    
    
    private final class HalfOpenState implements CircuitBreakerState {
        private double chance = 0.05;  // 5% will be passed through

        @Override
        public boolean isRequestAllowed() {
            if(!policy.isHealthy(scope)) return random.nextDouble() <= chance;
            else{
                LOGGER.info("Circuit breaker state change state=closed host=" + scope);
                return changeState(new ClosedState()).isRequestAllowed();
            }
//            return (policy.isHealthy(scope)) ? changeState(new ClosedState()).isRequestAllowed()
//                                             : (random.nextDouble() <= chance);
        }
    }

    
    
    
    private CircuitBreakerState changeState(CircuitBreakerState newState) {
        state.set(newState);
        return newState;
    }
    
    private interface CircuitBreakerState {
        boolean isRequestAllowed();       
    }
    
    

    
    private class CachedCircuitBreakerPolicy implements HealthPolicy  {
        
        private final HealthPolicy healthPolicy;
        private final Duration cacheTtl;
        
        private final Cache<String, CachedResult> cache = CacheBuilder.newBuilder().maximumSize(1000).build();
        
        
        public CachedCircuitBreakerPolicy(HealthPolicy healthPolicy, Duration cacheTtl) {
            this.healthPolicy = healthPolicy;
            this.cacheTtl = cacheTtl;
        }
        
        
        @Override
        public boolean isHealthy(String scope) {
            
            CachedResult cachedResult = cache.getIfPresent(scope);
            if ((cachedResult == null) || (cachedResult.isExpired())) {
                cachedResult = new CachedResult(healthPolicy.isHealthy(scope), cacheTtl);
                cache.put(scope, cachedResult);
            }

            return cachedResult.isHealthy();
        }  
        
        
        private class CachedResult {
            private final boolean isHealthy;
            private Instant validTo;
            
            
            CachedResult(boolean isHealthy, Duration ttl) {
                this.isHealthy = isHealthy;
                validTo = Instant.now().plus(ttl);
            }
            
            
            public boolean isExpired() {
                return Instant.now().isAfter(validTo);
            }
            
            public boolean isHealthy() {
                return isHealthy;
            }
        }
    }
}

