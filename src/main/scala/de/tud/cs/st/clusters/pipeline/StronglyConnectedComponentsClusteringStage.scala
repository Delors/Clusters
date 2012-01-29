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
package pipeline

import scala.collection.mutable.Map
import framework.pipeline.ConfigurableClusteringStage
import framework.pipeline.ClusteringStageConfiguration
import framework.structure.Cluster
import framework.structure.Node
import framework.structure.util.ClusterManager
import graphscan.GraphScanResultBean
import graphscan.GraphScanningAlgorithms

/**
 * @author Thomas Schlosser
 *
 */
trait StronglyConnectedComponentsClusteringStage extends ConfigurableClusteringStage[StronglyConnectedComponentsClusteringStageConfiguration] {

    override def performClustering(cluster: Cluster): Cluster = {
        // calculate finishing times of all nodes using depth first search
        var result = GraphScanningAlgorithms.graphScanComplete(
            cluster, null, true, null)

        // calculate depth first search on the transposed cluster considering
        // the finishing times of the first run of the depth first search algorithm
        result = GraphScanningAlgorithms.graphScanComplete(cluster,
            null, true, result.order)(true)

        // create resulting clusters
        val inputNodes = cluster.getNodes.toArray
        cluster.clearNodes()
        // The first element of a cluster is buffered in this map.
        // If it remains the only element in that "cluster", no new cluster will be created.
        // As soon as the second element of that cluster occurs, the cluster will be created. 
        var newClusterMinSizeBuffer = Map[Int, Node]()
        var resultMap = Map[Int, Cluster]()
        for (node ← inputNodes) {
            val sccID = result.color(node.uniqueID) - 2
            if (sccID >= 0) {
                resultMap.get(sccID) match {
                    case Some(c) ⇒
                        c.addNode(node)
                    case None ⇒
                        newClusterMinSizeBuffer.get(sccID) match {
                            case Some(firstElement) ⇒
                                val c = clusterManager.createCluster("SCC_"+System.nanoTime(), this.stageName) //sccID, this.stageName)
                                c.addNode(firstElement)
                                c.addNode(node)
                                resultMap(sccID) = c
                                cluster.addNode(c)
                                newClusterMinSizeBuffer.remove(sccID)
                            case None ⇒
                                newClusterMinSizeBuffer(sccID) = node
                        }
                }
            }
        }
        newClusterMinSizeBuffer.values foreach {
            cluster.addNode(_)
        }
        cluster
    }
}

trait StronglyConnectedComponentsClusteringStageConfiguration extends ClusteringStageConfiguration {

}

class DefaultStronglyConnectedComponentsClusteringStage(
    val configuration: StronglyConnectedComponentsClusteringStageConfiguration)
        extends StronglyConnectedComponentsClusteringStage {
}
