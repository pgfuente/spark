/*
 * Copyright (c) 2015 Daniel Higuero.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spark.examples.events;

import org.apache.commons.math3.random.MersenneTwister;

import java.util.Date;

/**
 * Authorization event
 */
public class AuthEventGenerator implements IGenerator{

    /**
     * Processes that generate the auth entries.
     */
    private String [] processes = {"sshd", "httpd", "login"};

    /**
     * Number of hosts in the scenario.
     */
    private final int numberHosts;

    /**
     * Field separator.
     */
    private final String separator;

    /**
     * Random number generator.
     */
    private MersenneTwister twister = null;

    /**
     * Log message in case of successful authentication.
     */
    public static final String AUTH_OK = "Authorization OK";

    /**
     * Log message in case of failed authentication.
     */
    public static final String AUTH_FAIL = "Authorization failed";

    /**
     * Generator constructor.
     * @param numberHosts Number of hosts in the scenario.
     * @param separator field separator.
     */
    public AuthEventGenerator(int numberHosts, String separator){
        //Fixed seed to reproduce patterns.
        twister = new MersenneTwister(numberHosts);
        this.numberHosts = numberHosts;
        this.separator = separator;
    }

    /**
     * Generate a random auth log entry.
     * @param successful Whether the authorization should be successful or not.
     * @return A line with the log.
     */
    public String generateRandomEvent(boolean successful){
        StringBuilder sb = new StringBuilder(new Date().toString());
        sb.append(separator);
        sb.append("host").append(twister.nextInt(numberHosts)).append(separator);
        sb.append(processes[twister.nextInt(processes.length)]).append(separator);
        if(successful){
            sb.append(AUTH_OK);
        }else{
            sb.append(AUTH_FAIL);
        }
        return sb.toString();
    }

}
