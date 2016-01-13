package com.gmv.mcd.model;

import java.sql.Date
import org.apache.spark.mllib.linalg.{ Vectors, Vector }

case class Transaction( msisdn: Option[String], time: Date, lines: String, vector: Vector, cluster: Int, distance: Double)