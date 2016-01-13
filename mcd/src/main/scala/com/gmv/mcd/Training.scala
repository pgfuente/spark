package com.gmv.mcd

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.log4j.{ Level, Logger }
import java.sql.Date
import org.apache.spark.mllib.linalg.{ Vectors, Vector }
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.SparkConf
import org.apache.spark.sql.SaveMode
import com.databricks.spark.csv._
import com.gmv.mcd.util.ParseUtils
import com.gmv.mcd.util.Optimizer
import org.apache.spark.mllib.clustering.GaussianMixture
import org.apache.spark.mllib.stat.MultivariateStatisticalSummary
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.mllib.feature.PCA
import org.alitouka.spark.dbscan.DbscanSettings
import org.alitouka.spark.dbscan.Dbscan
import org.alitouka.spark.dbscan.spatial.Point
import org.alitouka.spark.dbscan.util.io.IOHelper
import org.alitouka.spark.dbscan.exploratoryAnalysis.DistanceToNearestNeighborDriver

/**
 * Hello world!
 *
 */
object Training {

  def main(args: Array[String]) {
    //Suppress Spark output
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    val excluded = Seq("") //Seq("34671705042")

    //Define the Spark configuration. In this case we are using the local mode
    //val sparkConf = new SparkConf().setMaster("local[1]").setAppName("PAGF TFM")

    val logFile = "C:\\cobre\\cobre\\*1\\cobre.log.2015-08-05*"
    val typesFile = "cobre/types"

    val sc = new SparkContext("local[4]", "Simple", "$SPARK_HOME", List("target\\mcd-0.0.1-SNAPSHOT.jar"))
    val file = sc.textFile(logFile)

    println("Launching LogProcessingApp with "+file.countApprox(100000, 0.95)+ " lines")

    val typesCount = file.map(ParseUtils.removeVariableData).filter(x => !(x.trim().startsWith("at"))).countByValue()
    val threshold = typesCount.values.reduce((x, y) => Math.max(x, y)) * 0.01
    val types = typesCount.filter(p => p._2 > threshold).map(x => x._1).toList.sorted

    sc.parallelize(types).repartition(1).saveAsTextFile(typesFile)

    println("Types found: " + types.length)

    val transactions = file.map(x => (ParseUtils.transactionpattern.findFirstIn(x), x))
      .filter(x => x._1 != None).filter(x => ParseUtils.timepattern.findFirstIn(x._2) != None)
      .groupByKey()

    //"Grouped by transaction"
      
    val transactionsByMsisdn = transactions.map(x => (ParseUtils.msisdnpattern.findFirstIn(x._2.mkString), x._2))
      .filter(x => !(x._1 == None || excluded.contains(x._1.get)))
      .groupByKey()

    println("Transactions loaded")

    val trainingVectors = transactionsByMsisdn.map(x => { (x._1.get, ParseUtils.vectorize(x._2, types)) }).cache()

    // Compute column summary statistics.
    val summary: MultivariateStatisticalSummary = Statistics.colStats(trainingVectors.map(_._2))
    println("Variance ACC:" + summary.variance.toArray.toList.reduce(_+_)) // column-wise variance
    
    /* Range of dimensions */
    val dimRange = (types.length to 1 by -1)
    val d = Optimizer.optimizeDimensions(trainingVectors.map(_._2), dimRange, 0.9)

    // Compute the top principal components.
    val pca = new PCA(d).fit(trainingVectors.map(_._2))
    
    val pca_matrix = sc.parallelize(List(pca.pc.numRows,pca.pc.numCols,pca.pc.values.toList))
    pca_matrix.repartition(1).saveAsTextFile("cobre/models/pca")

    // Project vectors to the linear space spanned by the top principal components, keeping the label
    val projected = trainingVectors.map(p => p.copy(_2 = pca.transform(p._2)))
    
    // Compute column summary statistics.
    val summary_pca: MultivariateStatisticalSummary = Statistics.colStats(projected.map(_._2))
    println("PCA("+d+") Variance ACC:" + summary_pca.variance.toArray.toList.reduce(_+_)) // column-wise variance
    
//    val clusteringSettings = new DbscanSettings().withEpsilon(25).withNumberOfPoints(1000)
//    val dbscan = Dbscan.train(projected.map(x => new Point(x._2.toArray)), clusteringSettings)
//    IOHelper.saveClusteringResult(dbscan, "/cobre/dbscan")

    val numIterations = 1000

    /* Range of cluster center */
    val clusterRange = (1 to 49 by 5)

    val k = Optimizer.optimizeByDistance(projected, clusterRange, numIterations)

    println("Optimized k=" + k)

    val model = KMeans.train(projected.map(x => x._2), k, numIterations*100)

    if (k > 1)
      println("Distance between centers:" + Optimizer.distance(model.clusterCenters(0), model.clusterCenters(1)))

    model.save(sc, "cobre/models/k-means")

    println("Finished")
    sc.stop()
  }
}