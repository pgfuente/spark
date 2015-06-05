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
public class WebEventGenerator implements IGenerator{

    /**
     * Request methods.
     */
    private String [] methods = {"GET", "POST"};

    /**
     * Processes that generate the auth entries.
     */
    private String [] urls = {"index.html", "catalog/", "catalog/search"};

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
     * Successful HTTP request.
     */
    private static final int REQ_OK = 200;

    /**
     * Failed HTTP request.
     */
    private static final int REQ_FAIL = 402;

    /**
     * Generator constructor.
     * @param numberHosts Number of hosts in the scenario.
     * @param separator field separator.
     */
    public WebEventGenerator(int numberHosts, String separator){
        //Fixed seed to reproduce patterns.
        twister = new MersenneTwister(numberHosts);
        this.numberHosts = numberHosts;
        this.separator = separator;
    }

    /**
     * Generate a random web log entry.
     * @param successful Whether the request should be successful or not.
     * @return A line with the log.
     */
    public String generateRandomEvent(boolean successful){
        StringBuilder sb = new StringBuilder();
        sb.append("host").append(twister.nextInt(numberHosts)).append(separator);
        sb.append(new Date().toString()).append(separator);
        sb.append(methods[twister.nextInt(methods.length)]).append(separator);
        sb.append(urls[twister.nextInt(urls.length)]).append(separator);
        if(successful){
            sb.append(REQ_OK);
        }else{
            sb.append(REQ_FAIL);
        }
        return sb.toString();
    }

}
