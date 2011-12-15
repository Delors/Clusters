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

import framework.filter.ClusterFilter
import framework.filter.IntermediateClusterFilter
import framework.structure.Cluster
import framework.structure.TypeNode
import framework.structure.FieldNode
import framework.structure.MethodNode
import framework.structure.NodeCloner
import framework.structure.util.ClusterBuilder

/**
 * @author Thomas Schlosser
 *
 */
class LayerClustering(
        val builder: ClusterBuilder,
        val successorFilter: Option[ClusterFilter],
        val clusterNewFilter: Option[ClusterFilter]) extends IntermediateClusterFilter {

    protected override def process(cluster: Cluster): Cluster = {
        val result = NodeCloner.createCopy(cluster)
        val topLayer = builder.createCluster("top")
        val middleLayer = builder.createCluster("middle")
        val bottomLayer = builder.createCluster("bottom")
        result.addNode(topLayer)
        result.addNode(middleLayer)
        result.addNode(bottomLayer)
        for (node ← cluster.getNodes) {
            val inDegree = node.getTransposedEdges.length
            val outDegree = node.getEdges.length
            val copy = NodeCloner.createDeepCopy(node)
            if (inDegree == 0 && outDegree > 0)
                topLayer.addNode(copy)
            else if (inDegree > 0 && outDegree > 0)
                middleLayer.addNode(copy)
            else if (inDegree > 0 && outDegree == 0)
                bottomLayer.addNode(copy)
            else if (inDegree == 0 && outDegree == 0)
                result.addNode(copy)
        }
        if (clusterNewFilter.isDefined) {
            val newTopLayer = clusterNewFilter.get.process(Array(topLayer))
            val newMiddleLayer = clusterNewFilter.get.process(Array(middleLayer))
            val newBottomLayer = clusterNewFilter.get.process(Array(bottomLayer))
            result.removeNode(topLayer.uniqueID)
            result.removeNode(middleLayer.uniqueID)
            result.removeNode(bottomLayer.uniqueID)
            result.addNode(newTopLayer(0))
            result.addNode(newMiddleLayer(0))
            result.addNode(newBottomLayer(0))
        }
        result
    }
}

object LayerClustering {

    def apply(
        clusterBuilder: ClusterBuilder,
        successorFilter: ClusterFilter = null,
        clusterNewFilter: ClusterFilter = null): LayerClustering =
        new LayerClustering(
            clusterBuilder,
            if (successorFilter == null) None else Some(successorFilter),
            if (clusterNewFilter == null) None else Some(clusterNewFilter))

}
