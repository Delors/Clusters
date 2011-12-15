/* License (BSD Style License):
*  Copyright (c) 2009, 2011
*  Software Technology Group
*  Department of Computer Science
*  Technische Universität Darmstadt
*  All rights reserved.
*
*  Redistribution and use in source and binary forms, with or without
*  modification, are permitted provided that the following conditions are met:
*
*  - Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
*  - Redistributions in binary form must reproduce the above copyright notice,
*    this list of conditions and the following disclaimer in the documentation
*    and/or other materials provided with the distribution.
*  - Neither the name of the Software Technology Group or Technische 
*    Universität Darmstadt nor the names of its contributors may be used to 
*    endorse or promote products derived from this software without specific 
*    prior written permission.
*
*  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
*  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
*  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
*  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
*  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
*  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
*  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
*  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
*  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
*  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
*  POSSIBILITY OF SUCH DAMAGE.
*/
package de.tud.cs.st.clusters
package filter
package similarity

import scala.collection.mutable.Map
import framework.filter.ClusterFilter
import framework.filter.IntermediateClusterFilter
import framework.structure.Cluster
import framework.structure.Node
import framework.structure.NodeCloner
import framework.structure.util.ClusterBuilder
import de.tud.cs.st.bat.resolved.dependency.DependencyType._

/**
 * @author Thomas Schlosser
 *
 */
class AgglomerativeHierarchicalClustering(
    val builder: ClusterBuilder,
    val successorFilter: Option[ClusterFilter],
    val clusterNewFilter: Option[ClusterFilter])
        extends IntermediateClusterFilter {

    val simThreshold = 0.2

    protected override def process(cluster: Cluster): Cluster = {
        //        println(BinaryDependencyFeatureSelector.selectFeatures(cluster))
        val result = NodeCloner.createCopy(cluster)
        val featuresMap: Map[Node, Map[DependencyType, Int]] = BinaryDependencyFeatureSelector.selectFeatures(cluster)
        var similarities = JacardMetric.calcSimilarities(featuresMap)
        var sortedNodes = similarities.elements.toList.sort((e1, e2) ⇒ (e1._2 > e2._2))
        println(sortedNodes mkString "\n")
        var maxSim = sortedNodes(0)._2
        while (maxSim >= simThreshold) {
            sortedNodes match {
                case mostSimilarElement :: tail ⇒
                    val node1 = builder.getNode(mostSimilarElement._1._1)
                    val node2 = builder.getNode(mostSimilarElement._1._2)
                    val newCluster = builder.createCluster("AHC_Cluster")
                    newCluster.addNode(NodeCloner.createDeepCopy(node1))
                    newCluster.addNode(NodeCloner.createDeepCopy(node2))
                    result.addNode(newCluster)
                    //TODO add new Cluster with its similarities to other nodes or clusters, respectively,
                    // to the correct position in the list...
                    // similarity values to node1 and node2 have to be removed...
                    val features1 = featuresMap.removeKey(node1)
                    val features2 = featuresMap.removeKey(node2)
                    similarities = JacardMetric.calcSimilarities(featuresMap)
                    CompleteLinkage.calcSimilarities(JacardMetric, features1.get, features2.get, featuresMap)
                    //                    Map[(Int, Int), Double]
                    sortedNodes = tail
                case Nil ⇒ // No further nodes to merge...terminate
                    maxSim = Double.NegativeInfinity
            }
        }

        //        if (clusterNewFilter.isDefined)
        //            clusterNewFilter.get.process(Array(topLayer, middleLayer, bottomLayer))
        //TODO: return correct result (copy of the nodes...)
        result
    }
}

object AgglomerativeHierarchicalClustering {

    def apply(clusterBuilder: ClusterBuilder,
              successorFilter: ClusterFilter = null,
              clusterNewFilter: ClusterFilter = null): AgglomerativeHierarchicalClustering =
        new AgglomerativeHierarchicalClustering(
            clusterBuilder,
            if (successorFilter == null) None else Some(successorFilter),
            if (clusterNewFilter == null) None else Some(clusterNewFilter))

}

trait Linkage {
    type FeaturesMap = Map[Node, Features]
    type Features = Map[DependencyType, Int]
    type Similarities = Map[(Int, Int), Double]

    def calcSimilarities(
        similarityMetric: SimilarityMetric,
        features1: Features,
        features2: Features,
        features: FeaturesMap): Similarities = {
        var result = Map[(Int, Int), Double]()
        for ((nodeX, featuresX) ← features) {
            //FIXME         result((nodeX.uniqueID, cluster.uniqueID)) = calcSimilarity(featuresX, features1, feature2)
        }
        result
    }

    def calcSimilarity(similarityMetric: SimilarityMetric, featuresX: Features, features1: Features, features2: Features): Double
}

object CompleteLinkage extends Linkage {
    override def calcSimilarity(
        similarityMetric: SimilarityMetric,
        featuresX: Features,
        features1: Features,
        features2: Features): Double = {
        //FIXME        scala.Math.min(similarityMetric.calcSimilarity())
        42.0
    }
}
