package org.spark.examples.streaming

/**
 * Web Event.
 */

object WebEvent {

}

case class WebEvent(sourceHost: String, timestamp: String, method: String, url: String, httpCode: String)