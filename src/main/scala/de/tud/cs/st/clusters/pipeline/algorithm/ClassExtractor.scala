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
package algorithm

import framework.pipeline.ClusteringAlgorithm
import scala.collection.mutable.Map
import framework.structure.Cluster
import framework.structure.Node
import framework.structure.TypeNode
import framework.structure.FieldNode
import framework.structure.MethodNode

/**
 *
 *
 * @author Thomas Schlosser
 *
 */
class ClassExtractor extends ClusteringAlgorithm {

    protected def doPerformClustering(cluster: Cluster): Boolean = {
        var createdNewCluster = false
        val clustersMap = Map[String, Set[Node]]()
        var ignoredClusters: Set[Node] = Set()

        for (node ← cluster.nodes) {
            if (node.isCluster) {
                ignoredClusters = ignoredClusters + node
            }
            else {
                var typeIdentifier: String = null
                node match {
                    case t: TypeNode ⇒
                        typeIdentifier = t.identifier
                    case f: FieldNode ⇒
                        typeIdentifier = f.identifier.substring(0, f.identifier.lastIndexOf('.'))
                    case m: MethodNode ⇒
                        typeIdentifier = m.identifier.substring(0, m.identifier.lastIndexOf('('))
                        typeIdentifier = typeIdentifier.substring(0, typeIdentifier.lastIndexOf('.'))
                }
                val clusterSet = clustersMap.getOrElse(typeIdentifier, Set())
                clustersMap(typeIdentifier) = clusterSet + node
            }
        }

        cluster.clearNodes()
        for ((clusterIdentifier, nodeSet) ← clustersMap) {
            val sameNeighborCluster = clusterManager.createCluster(clusterIdentifier, this.stageName)
            createdNewCluster = true
            nodeSet foreach {
                sameNeighborCluster.addNode(_)
            }
            cluster.addNode(sameNeighborCluster)
        }
        // re-add all ignored clusters to the result cluster
        ignoredClusters foreach {
            cluster.addNode(_)
        }

        createdNewCluster
    }

}
