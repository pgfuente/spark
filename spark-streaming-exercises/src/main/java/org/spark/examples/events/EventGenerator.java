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

import org.apache.commons.cli.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class generates two streams of events in two separate ports. By
 * default, port 10001 will contain auth related information, while port 10002
 * will contain web server related information.
 */
public class EventGenerator {

    /**
     * Separator between the elements of an event.
     */
    private static final String SEPARATOR = ";";

    /**
     * Number of hosts that are simulated in the events.
     */
    private static final int NUMBER_HOSTS = 10;

    /**
     * Number of iterations.
     */
    private final int numberIterations;

    /**
     * Number of events per iteration that are ok.
     */
    private final int numberEventsOK;

    /**
     * Number of events per iteration that should be suspicious.
     */
    private final int numberSuspiciousEvents;

    /**
     * Time to sleep between messages.
     */
    private final long sleepTime;

    /**
     * Generator of authentication-related events.
     */
    private final IGenerator authGenerator;

    /**
     * Generator of web-related events.
     */
    private final IGenerator webGenerator;

    /**
     * Class constructor.
     * @param numberIterations Number of iterations.
     * @param numberEventsOK Number of events that are ok per iteration.
     * @param numberSuspiciousEvents Number of suspicious events per iteration.
     * @param sleepTime Time to sleep between messages.
     */
    public EventGenerator(int numberIterations,
                          int numberEventsOK,
                          int numberSuspiciousEvents,
                          long sleepTime){
        this.numberIterations = numberIterations;
        this.numberEventsOK = numberEventsOK;
        this.numberSuspiciousEvents = numberSuspiciousEvents;
        this.sleepTime = sleepTime;
        System.out.println("Launching EventGenerator");
        System.out.println("Number iterations: " + this.numberIterations);
        System.out.println("Number Events OK/it: " + this.numberEventsOK);
        System.out.println("Number Events Fail/it: " + this.numberSuspiciousEvents);
        System.out.println("Sleep time: " + this.sleepTime);

        authGenerator = new AuthEventGenerator(NUMBER_HOSTS, SEPARATOR);
        webGenerator = new WebEventGenerator(NUMBER_HOSTS, SEPARATOR);
    }

    /**
     * Launch the generator.
     * @param authPort Port for authentication related information.
     * @param webPort Port for web related information.
     */
    public void startGenerator(int authPort, int webPort){
        ServerSocket authSocket = null;
        ServerSocket webSocket = null;
        Socket authClient = null;
        Socket webClient = null;
        try {
            System.out.println("Waiting for clients ...");
            authSocket = new ServerSocket(authPort);
            webSocket = new ServerSocket(webPort);
            authClient = authSocket.accept();
            System.out.println("Auth client connected");
            webClient = webSocket.accept();
            System.out.println("Web client connected");
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendEvents(authClient, webClient);

        try {
            if(authClient != null){
                authClient.close();
            }
            if(webClient != null){
                webClient.close();
            }
            if(authSocket != null){
                authSocket.close();
            }
            if(webSocket != null){
                webSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Send the events to the client.
     * @param authClient Socket where the client will receive authentication related events.
     * @param webClient Socket where the client will receive web related events.
     */
    private void sendEvents(Socket authClient, Socket webClient){
        PrintWriter authOut = null;
        PrintWriter webOut = null;
        try {
            System.out.println("Sending web & auth information");

            authOut = new PrintWriter(authClient.getOutputStream(), true);
            webOut = new PrintWriter(webClient.getOutputStream(), true);

            int ok = 0;
            int suspicious = 0;
            for(int it = 0; it < numberIterations; it++){
                System.out.println("Iteration: " + it);
                for(ok = 0; ok < numberEventsOK; ok++){
                    authOut.println(authGenerator.generateRandomEvent(true));
                    webOut.println(webGenerator.generateRandomEvent(true));
                    System.out.print(".");
                    sleep();
                }
                for(suspicious = 0; suspicious < numberSuspiciousEvents; suspicious++){
                    authOut.println(authGenerator.generateRandomEvent(false));
                    webOut.println(webGenerator.generateRandomEvent(false));
                    System.out.print("#");
                    sleep();
                }
                System.out.println();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            authOut.close();
            webOut.close();
        }

        System.out.println("All messages sent!");

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sleep between messages.
     */
    private void sleep(){
        try {
            Thread.sleep(this.sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the set of options that are accepted when launching the API.
     *
     * @return A {@link Options}.
     */
    private static Options getOptions() {
        final Options opt = new Options();
        opt.addOption(Option.builder("help").desc("Show help").build());
        opt.addOption(Option.builder("numberIterations").desc("Number of iterations")
                .hasArg(true).argName("numberIterations").build());
        opt.addOption(Option.builder("numberEventsOK").desc("Number of successful events per iteration.")
                .hasArg(true).argName("numberEventsOK").build());
        opt.addOption(Option.builder("numberEventsFail").desc("Number of suspicious events per iteration.")
                .hasArg(true).argName("numberEventsFail").build());
        opt.addOption(Option.builder("sleepTime").desc("Time to sleep between sending new events.")
                .hasArg(true).argName("sleepTime").build());
        return opt;
    }

    /**
     * Show the help.
     */
    private static void showHelp() {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(EventGenerator.class.getName(), getOptions());
        System.exit(-1);
    }

    /**
     * Launch the Event generator on ports 10001 and 10002.
     * @param args No arguments required.
     */
    public static void main(String [] args){
        final CommandLineParser parser = new DefaultParser();
        try {
            final CommandLine cmd = parser.parse(getOptions(), args);
            if (cmd.hasOption("help")) {
                showHelp();
            } else {

                int numberIterations = Integer.valueOf(cmd.getOptionValue("numberIterations", "10"));
                int numberEventsOK = Integer.valueOf(cmd.getOptionValue("numberEventsOK", "10"));
                int numberSuspiciousEvents = Integer.valueOf(cmd.getOptionValue("numberEventsFail", "10"));
                long sleepTime = Long.valueOf(cmd.getOptionValue("sleepTime", "100"));

                EventGenerator generator = new EventGenerator(
                        numberIterations, numberEventsOK, numberSuspiciousEvents, sleepTime);
                generator.startGenerator(10001, 10002);
            }
        } catch (ParseException e) {
            System.err.println("Error parsing options.");
            showHelp();
        }

    }

}
