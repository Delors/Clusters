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
import framework.pipeline.Clustering
import framework.pipeline.IntermediateClustering
import framework.structure.Cluster
import framework.structure.Node
import framework.structure.util.NodeManager
import graphscan.GraphScanResultBean
import graphscan.GraphScanningAlgorithms

/**
 * @author Thomas Schlosser
 *
 */
class StronglyConnectedComponentsClustering(
    val nodeManager: NodeManager,
    val successorClustering: Option[Clustering],
    val newClusterClustering: Option[Clustering])
        extends IntermediateClustering {

    protected override def process(cluster: Cluster): Cluster = {
        // calculate finishing times of all nodes using depth first search
        var result = GraphScanningAlgorithms.graphScanComplete(
            cluster, null, true, null)

        // calculate depth first search on the transposed cluster considering
        // the finishing times of the first run of the depth first search algorithm
        result = GraphScanningAlgorithms.graphScanComplete(cluster,
            null, true, result.order)(true)

        // create resulting clusters
        val resultCluster = nodeManager.createCopy(cluster)
        var resultMap = Map[Int, Cluster]()
        for (node ← cluster.getNodes) {
            val sccID = result.color(node.uniqueID) - 2
            if (sccID >= 0) {
                resultMap.get(sccID) match {
                    case Some(c) ⇒
                        c.addNode(nodeManager.createDeepCopy(node))
                    case None ⇒
                        val c = nodeManager.createCluster("SCC_"+System.nanoTime()) //sccID)
                        c.addNode(nodeManager.createDeepCopy(node))
                        resultMap(sccID) = c
                        resultCluster.addNode(c)
                }
            }
        }
        resultCluster
    }
}

object StronglyConnectedComponentsClustering {

    def apply(
        nodeManager: NodeManager,
        successorClustering: Clustering = null,
        newClusterClustering: Clustering = null): StronglyConnectedComponentsClustering =
        new StronglyConnectedComponentsClustering(
            nodeManager,
            if (successorClustering == null) None else Some(successorClustering),
            if (newClusterClustering == null) None else Some(newClusterClustering))
}