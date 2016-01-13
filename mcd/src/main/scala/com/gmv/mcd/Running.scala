package com.gmv.mcd

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.log4j.{ Level, Logger }
import java.sql.Date
import org.apache.spark.mllib.linalg.{ Vectors, Vector }
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.sql.{ Row, SQLContext }
import org.apache.spark.SparkConf
import org.apache.spark.sql.SaveMode
import com.databricks.spark.csv._
import com.gmv.mcd.util.ParseUtils
import com.gmv.mcd.model.Transaction
import org.apache.spark.mllib.clustering.KMeansModel
import com.gmv.mcd.util.Optimizer
import org.apache.spark.mllib.linalg.DenseMatrix

/**
 * Hello world!
 *
 */
object Running {

  def main(args: Array[String]) {
    //Suppress Spark output
    //Logger.getLogger("org").setLevel(Level.ERROR)
    //Logger.getLogger("akka").setLevel(Level.ERROR)

    //val excluded = Seq("34671705042")
    val excluded = Seq("")

    //Define the Spark configuration. In this case we are using the local mode
    //val sparkConf = new SparkConf().setMaster("local[1]").setAppName("PAGF TFM")

    val logFile = "C:\\cobre\\cobre\\*\\cobre.log.2015-07-3*"
    val typesFile = "cobre/types"

    val sc = new SparkContext("local[4]", "Simple", "$SPARK_HOME", List("target\\mcd-0.0.1-SNAPSHOT.jar"))
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._
    val file = sc.textFile(logFile)

    val dateformatter = new java.text.SimpleDateFormat("dd MMM yyyy HH:mm:ss,SSS")

     println("Launching LogProcessingApp with "+file.countApprox(100000, 0.95)+ " lines")

    var types = sc.textFile(typesFile).collect().toList.sorted

    println("Types found: " + types.length)

    val transactions = file.map(x => (ParseUtils.transactionpattern.findFirstIn(x), x))
      .filter(x => x._1 != None).filter(x => ParseUtils.timepattern.findFirstIn(x._2) != None)
      .groupByKey()

    //"Grouped by transaction"

    val transactionsByMsisdn = transactions.map(x => (ParseUtils.msisdnpattern.findFirstIn(x._2.mkString), x._2))
      .filter(x => !(x._1 == None || excluded.contains(x._1.get)))
      .groupByKey()

    //"Grouped by transaction and msisdn"

    println("Transactions loaded")

    val model = KMeansModel.load(sc, "cobre/models/k-means")
    //val gmm = GaussianMixtureModel.load(sc, "models/gmm")

    val pca_model = sc.textFile("cobre/models/pca").collect()

    val pca_rows = pca_model(0).toInt
    val pca_cols = pca_model(1).toInt
    val pca_values = pca_model(2).stripPrefix("List(").stripSuffix(")").split(",").map(x => x.toDouble)
    val pca = new DenseMatrix(pca_rows, pca_cols, pca_values).transpose

    val processedTransactions = transactionsByMsisdn.map(x => {
      val lines = x._2.mkString
      val msisdn = ParseUtils.msisdnpattern.findFirstIn(lines)
      val date = new Date(dateformatter.parse(ParseUtils.timepattern.findFirstIn(lines).get).getTime)
      val vector = pca.multiply(ParseUtils.vectorize(x._2, types))
      val cluster = model.predict(vector)
      val distance = Optimizer.distance(vector, model.clusterCenters(cluster))
      Transaction(msisdn, date, "", vector, cluster, distance)
    })

    if (false) {
      val total = processedTransactions.count()
      val quantiles = processedTransactions.sortBy(t => t.distance, true)
        .zipWithIndex().filter(x => (x._2 == total / 4 || x._2 == 3 * total / 4)).collect()
      val q1 = quantiles.filter(x => x._2 == total / 4).head._1.distance
      val q3 = quantiles.filter(x => x._2 == 3 * total / 4).head._1.distance
      val iqr = q3 - q1
      val outlier = q3 + 3 * iqr
      println("Q1:" + q1 + ", Q3:" + q3 + ", outlier:" + outlier)
      val outliers = processedTransactions.filter(x => x.distance > outlier).toDF
      outliers.show(10)
      outliers.repartition(1).saveAsCsvFile("cobre/outliers")
    } else {
      processedTransactions.toDF.repartition(1).saveAsCsvFile("cobre/distances")
    }

    println("Finished")
    sc.stop()
  }
}
