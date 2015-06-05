# spark-streaming-exercises

This project provides a set of utils in order to develop different exercises using Spark Streaming.

Currently, the project contains only a log event generator.

## Event Generator

The Event Generator produces a series of log events in plain text through common OS sockets. The idea behind this
component is to simulate two sources of log information based on the contents of the classical auth.log and access.log.
The generator is configured by specifying the number of iterations to go through, and the number of success and failed
log entries per iteration. This allows the listening Spark Streaming to develop different solutions to
determine which traffic should trigger an alarm.

To use the event generator with the default options execute:

    mvn compile exec:java -Dexec.mainClass=org.spark.examples.events.EventGenerator

Additionally , you can launch the generator with custom options.

    mvn compile exec:java -Dexec.mainClass=org.spark.examples.events.EventGenerator -Dexec.args="-numberIterations 20 -numberEventsOK 20 -numberEventsFail 80 -sleepTime 50"

When the generator is launched, it will wait for two clients to connect. Clients connecting to port _10001_ will receive
authorization entries. Clients connecting to port _10002_ will receive access log entries. Notice that the generator will
not send any information unless both clients are connected.

To facilitate the processing, each log entry has the fields separated by _";"_

### Auth events

An auth event is composed of the following fields:

  - Timestamp
  - Source host
  - Process
  - Message

### Web events

A web event is composed of the following fields:

  - Source host
  - Timestamp
  - Method
  - URL
  - HTTP code

## Proposed exercises

The following list of exercises can be done using the aforementioned Event Generator.

### Exercise 1

Create two case classes _AuthEvent_ and _WebEvent_ in order to transform incoming plain text into usefull object that
can be easily processed. Given both streams, process them in order to obtain with a window of 10 seconds:

 - Number of authentication events.
 - Number of web requests.
 - Number of successful authentication events
 - Number of failed authentication events.
 - Number of successful web requests.
 - Number of failed web requests.

 Print the results on screen as in the following example:

```
Number of authentication events : 31
Number of web requests: 31
Number of successful authentication events: 11
Number of failed authentication events: 20
Number of successful web requests: 11
Number of failed web requests: 20
```
### Exercise 2

Using the web request event source, count the number of events in a sliding window of length 10 seconds and a sliding
length of 5 seconds.

```
Number of web request in the last 10 seconds with slide: 20
```

## Exercise 3

Given a window of 10 seconds and the auth stream, determine which hosts could be attacking the system. In order to do
that, aggregate the hosts counting the number of failed requests and filter those that have at
least _ThresholdAuth_ requests.

```
Number of auth attacks in the last 10 seconds: 7
```

Internally, store the information as pairs of the type *(host_i, numberRequests)*.

## Exercise 4

Given the previous stream obtained in Exercise 3, filter those hosts that are already blacklisted in the system.
In order to do that, generate an RDD with the contents of a file that contains:

```
host0
host1
host2
host3
host4
```

As the output of the stream, maintain the same structure *(host_i, numberRequests)*. Print the results.

## Exercise 5

Calculate the accumulated number of web requests per host from *t=0* to the current time. Internally, use the same
structure as before *(host_i, totalNumberRequests)*. Show all elements first, and then show the _top 5_ hosts
with more requests.

```
(host2,103)
(host9,101)
(host4,85)
(host5,77)
(host7,61)
```

## Exercise 6

Join both auth and web streams to determine the hosts receiving the highest number of successful requests, and the
 hosts with the higher number of attacks. Use a window of 10 seconds.

# Resources

The following resources may help during the coding:

 - [Spark Streaming Programming Guide](https://spark.apache.org/docs/latest/streaming-programming-guide.html)
 - [Spark Streaming Examples](https://github.com/apache/spark/tree/master/examples/src/main/scala/org/apache/spark/examples/streaming)
 - [Spark Programming Guide](https://spark.apache.org/docs/latest/programming-guide.html)

