package org.spark.examples.streaming

/**
 * Auth Event.
 */
object AuthEvent {

}

case class AuthEvent(timestamp: String, sourceHost: String, process: String,
  message: String)

