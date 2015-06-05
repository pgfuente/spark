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

package org.spark.examples.streaming

import org.apache.log4j.{ Level, Logger }
import org.apache.spark.SparkConf
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Seconds
import org.apache.spark.storage.StorageLevel
import org.spark.examples.events.AuthEventGenerator
import org.apache.spark.streaming.Duration
import com.sun.org.apache.bcel.internal.generic.DADD
import java.util.Date
import java.util.Calendar
import java.text.SimpleDateFormat

/**
 * Code here the solution for the proposed exercises.
 */
object LogProcessingApp6 {

  /**
   * Field separator.
   */
  val Separator = ";";

  /**
   * Threshold that determines when a number of failed auth entries is considered an attack.
   */
  val ThresholdAuth = 1;

  /**
   * Threshold that determines when a number of failed web access entries is considered an attack.
   */
  val ThresholdWeb = 1;

  /**
   * Log message in case of successful authentication.
   */
  val AUTH_OK = "Authorization OK";

  /**
   * Log message in case of failed authentication.
   */
  val AUTH_FAIL = "Authorization failed";

  /**
   * Successful HTTP request.
   */
  val REQ_OK = "200";

  /**
   * Failed HTTP request.
   */
  val REQ_FAIL = "402";

  val dateFormat = new SimpleDateFormat("dd/MM/YY hh:mm:ss")

  def main(args: Array[String]) {

    //Suppress Spark output
    Logger.getLogger("org").setLevel(Level.FATAL)
    Logger.getLogger("akka").setLevel(Level.FATAL)

    println("Launching LogProcessingApp exercise 6...")

    //Define the Spark configuration. In this case we are using the local mode
    val sparkConf = new SparkConf().setMaster("local[4]").setAppName("Streaming Exercise")
    //Define a SparkStreamingContext with a batch interval of 10 seconds
    val ssc = new StreamingContext(sparkConf, Seconds(10))
    //Define checkpoint directory for updateStateByKey
    ssc.checkpoint(".")

    //Step 1 - Connect to the OS sockets.
    val authEventsRaw = ssc.socketTextStream("localhost", 10001, StorageLevel.MEMORY_AND_DISK_SER)
    val webEventsRaw = ssc.socketTextStream("localhost", 10002, StorageLevel.MEMORY_AND_DISK_SER)

    //Step 2 - Transform plain text into classes.
    val authEvents = authEventsRaw.map({ x =>
      {
        val fields = x.split(Separator)
        new AuthEvent(fields(0), fields(1), fields(2), fields(3))
      }
    })

    val webEvents = webEventsRaw.map({ x =>
      {
        val fields = x.split(Separator)
        new WebEvent(fields(0), fields(1), fields(2), fields(3), fields(4))
      }
    })

    //Step 3 - Apply window - Already windowed

    // Prepare known hosts RDD
    val hosts = ssc.sparkContext.parallelize(List("host0", "host1", "host2", "host3", "host4"))
    val knownHosts = hosts.map { x => (x, 0) }

    // Extract information

    val requestsOKByHost = webEvents.filter(x => x.httpCode.equals(REQ_OK))
      .map(x => (x.sourceHost, 1)) // Convert to tuple (SourceHost, 1)
      .updateStateByKey[Int]((newValues: Seq[Int], acc: Option[Int]) => Some(newValues.sum + acc.getOrElse(0))) // accumulate the attempts for same host 

    val authsOKByHost = authEvents.filter(x => x.message.equals(AUTH_OK))
      .map(x => (x.sourceHost, 1)) // Convert to tuple (SourceHost, 1)
      .updateStateByKey[Int]((newValues: Seq[Int], acc: Option[Int]) => Some(newValues.sum + acc.getOrElse(0))) // accumulate the attempts for same host 

    requestsOKByHost.join(authsOKByHost)
      .map(x => (x._1, x._2._1 + x._2._2))
      .foreachRDD(rdd => {
        println()
        println("Top 5 hosts by traffic:")
        rdd.sortBy(x => x._2, false).take(5)
          .foreach(x => println(x))
      })

    val requestsFAILByHost = webEvents.filter(x => x.httpCode.equals(REQ_FAIL))
      .map(x => (x.sourceHost, 1)) // Convert to tuple (SourceHost, 1)
      .updateStateByKey[Int]((newValues: Seq[Int], acc: Option[Int]) => Some(newValues.sum + acc.getOrElse(0))) // accumulate the attempts for same host 

    val authsFAILByHost = authEvents.filter(x => x.message.equals(AUTH_FAIL))
      .map(x => (x.sourceHost, 1)) // Convert to tuple (SourceHost, 1)
      .updateStateByKey[Int]((newValues: Seq[Int], acc: Option[Int]) => Some(newValues.sum + acc.getOrElse(0))) // accumulate the attempts for same host 

    requestsFAILByHost.join(authsFAILByHost)
      .map(x => (x._1, x._2._1 + x._2._2))
      .foreachRDD(rdd => {
        println()
        println("Top 5 hosts by attacks:")
        rdd.sortBy(x => x._2, false).take(5)
          .foreach(x => println(x))
      })

    //Start the streaming context
    ssc.start()
    ssc.awaitTermination()

  }

}
