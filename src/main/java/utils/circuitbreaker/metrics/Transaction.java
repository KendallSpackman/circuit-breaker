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
package utils.circuitbreaker.metrics;


import org.joda.time.Duration;
import org.joda.time.Instant;

public class Transaction {
    private final Instant startTime = Instant.now();

    private volatile Instant endTime = null;
    private volatile boolean isFailed = false;

    
    public Instant getStarttime() {
        return startTime;
    }
    
    public boolean isFailed() {
        return isFailed;
    }
    
    public boolean isRunning() {
        return (endTime == null);
    }
    
    public Duration getConsumedMillis() {
        if (endTime == null) {
            return new Duration(startTime, Instant.now());
        } else {
            return new Duration(startTime, endTime);
        }
    }
    
    public void close(boolean isFailed) {
        endTime = Instant.now();
        this.isFailed = isFailed;
    }
    
    
    public static void close(Object transaction, boolean isFailed) {
        if ((transaction != null) && (transaction instanceof Transaction)) {
            ((Transaction) transaction).close(isFailed);
        }
    }
}