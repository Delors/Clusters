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
package framework
package pipeline

import scala.collection.mutable.Map
import framework.structure.Cluster
import framework.structure.Node
import framework.structure.Edge
import de.tud.cs.st.bat.resolved.dependency._

/**
 * @author Thomas Schlosser
 *
 */
trait SameNeighborClustering extends Clustering {

    val edgeFilter: Int ⇒ Edge ⇒ Boolean = _ ⇒ _ ⇒ false

    val transposedEdgeFilter: Int ⇒ Edge ⇒ Boolean = _ ⇒ _ ⇒ false

    protected override def process(cluster: Cluster): Cluster = {
        def getConsideredEdge(node: Node): Option[Edge] = {
            node.getEdges.find(edge ⇒ isOfConsideredDependencyType(edge.dType))
        }

        val clustersMap = Map[Int, Set[Node]]()

        for (node ← cluster.getNodes) {
            getConsideredEdge(node) match {
                case Some(edge) ⇒
                    val neighborNodeID = edge.targetID
                    val clusterSet = clustersMap.getOrElse(neighborNodeID, Set())
                    clustersMap(neighborNodeID) = clusterSet + node
                case None ⇒
            }
        }

        cluster.clearNodes()
        var newClusters = Set[Cluster]()
        for ((neighborNodeID, nodeSet) ← clustersMap) {
            val neighborNode = clusterManager.getNode(neighborNodeID)
            val sameNeighborCluster = clusterManager.createCluster(neighborNode.identifier)
            sameNeighborCluster.addNode(neighborNode)
            nodeSet foreach {
                sameNeighborCluster.addNode(_) // node was cloned before it was put into map
            }
            cluster.addNode(sameNeighborCluster)
            newClusters = newClusters + sameNeighborCluster
        }

        cluster
    }

    protected def isOfConsideredDependencyType(dType: DependencyType): Boolean
}
