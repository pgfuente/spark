package com.gmv.mcd.util
/* Copyright (c) 2014 Dr. Krusche & Partner PartG
* 
* This file is part of the Spark-Outlier project
* (https://github.com/skrusche63/spark-outlier).
* 
* Spark-Outlier is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
* Spark-Outlier is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* Spark-Outlier. 
* 
* If not, see <http://www.gnu.org/licenses/>.
*/

import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.{ Vector, Vectors }
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.clustering.GaussianMixture
import scala.util.Sorting
import org.apache.spark.mllib.stat.MultivariateStatisticalSummary
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.mllib.feature.PCA

object Optimizer {

  /**
   * Determine from a range of cluster numbers that number where the mean
   * entropy of all cluster labels is minimal; note, that the entropy is
   * an indicator for the homogenity of the cluster labels
   */
  def optimizeByEntropy(data: RDD[(String, Vector)], range: Range, iterations: Int): Int = {

    val scores = range.par.map(k => (k, clusterEntropy(data, k, iterations))).toList
    scores.foreach(println)
    scores.sortBy(_._2).head._1

  }

  def clusterEntropy(data: RDD[(String, Vector)], clusters: Int, iterations: Int): Double = {

    val vectors = data.map(point => point._2)
    val model = KMeans.train(vectors, clusters, iterations)

    val entropies = data.map(point => {

      val cluster = model.predict(point._2)
      (cluster, point._1)

    }).groupBy(_._1).map(data => MathHelper.strEntropy(data._2.map(_._2))).collect()

    entropies.sum / entropies.size

  }

  /**
   * Determine from a range of cluster numbers that number where the mean
   * distance between cluster points and their cluster centers is minimal
   */
  def optimizeByDistance(data: RDD[(String, Vector)], range: Range, iterations: Int, method: String = "kmeans"): Int = {

    val vectors = data.map(x => x._2)
    val num_clusters = method match {
      case "kmeans" => range.takeWhile(k => clusterDistanceKmeans(vectors, k, iterations) > 1.0).end
      case "gmm" => range.takeWhile(k => clusterDistanceGMM(vectors, k, iterations) > 1.0).end
    }

    num_clusters
  }

  def distance(a: Array[Double], b: Array[Double]) =
    Math.sqrt(a.zip(b).map(p => p._1 - p._2).map(d => d * d).sum)

  def distance(a: Vector, b: Vector) =
    Vectors.sqdist(a, b)

  /**
   * This method calculates the mean distance of all data (vectors) from
   * their centroids, given certain clustering parameters; the method may
   * be used to score clusters
   */
  def clusterDistanceKmeans(data: RDD[Vector], clusters: Int, iterations: Int): Double = {

    val model = KMeans.train(data, clusters, iterations)
    /**
     * Centroid: Vector that specifies the centre of a certain cluster
     */
    val centroids = model.clusterCenters

    val distances = data.map(point => {

      val cluster = model.predict(point)
      val centroid = centroids(cluster)

      distance(centroid, point)

    }).collect()

    val average = distances.sum / distances.size

    println(clusters,average)

    average

  }

  def clusterDistanceGMM(data: RDD[Vector], clusters: Int, iterations: Int): Double = {

    println("Calculando modelo para K=" + clusters)
    val model = new GaussianMixture().setK(clusters).setMaxIterations(iterations).run(data)
    println("Calculando distancias para K=" + clusters)

    val distances = data.map(point => {

      val probabilities = model.gaussians.map(x => x.pdf(point))
      Sorting.quickSort(probabilities)
      1 / probabilities(0)
    }).collect()

    distances.sum / distances.size

  }

  def optimizeDimensions(vectors: RDD[Vector], range: Range, threshold: Double = 0.9): Int = {
    val summary: MultivariateStatisticalSummary = Statistics.colStats(vectors)
    val variance = summary.variance.toArray.toList.reduce(_ + _)
    println("variance obj:" + variance * threshold)

    range.takeWhile(d => varianceExplained(vectors, d) > (variance * threshold)).end
  }

  def varianceExplained(vectors: RDD[Vector], dimensions: Int): Double = {
    // Compute the top 10 principal components.
    val pca = new PCA(dimensions).fit(vectors)

    // Project vectors to the linear space spanned by the top 10 principal components, keeping the label
    val projected = vectors.map(pca.transform)

    // Compute column summary statistics.
    val summary_pca: MultivariateStatisticalSummary = Statistics.colStats(projected)

    val variance = summary_pca.variance.toArray.toList.reduce(_ + _)
    println(dimensions, variance)
    variance
  }

}