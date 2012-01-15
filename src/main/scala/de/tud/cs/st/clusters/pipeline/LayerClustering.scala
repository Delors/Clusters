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

import framework.pipeline.Clustering
import framework.structure.Cluster
import framework.structure.Node
import framework.structure.TypeNode
import framework.structure.FieldNode
import framework.structure.MethodNode
import framework.structure.util.ClusterManager

/**
 * @author Thomas Schlosser
 *
 */
class LayerClustering(val performRecursion: Boolean) extends Clustering {

    protected override def process(cluster: Cluster): Cluster = {
        var layer = 0

        def createLayers(nodes: Set[Node]) {
            def createNewLayerCluster(): Cluster = {
                val layerCluster = clusterManager.createCluster("layer_"+layer)
                layer += 1
                cluster.addNode(layerCluster)
                layerCluster
            }

            def createNewLayerClusterWithNodes(nodes: Iterable[Node]) {
                val layerCluster = createNewLayerCluster()
                nodes foreach { node ⇒
                    layerCluster.addNode(node)
                }
            }

            val nodeIDs = nodes map { _.uniqueID }

            var topLayerNodes = Set[Node]()
            var middleLayerNodes = Set[Node]()
            var bottomLayerNodes = Set[Node]()
            var sparatedNodes = Set[Node]()

            for (node ← nodes) {
                // only edges to nodes in 'nodes' should be considered for degree calculation
                // each edge group (edges with same source, target and dependency type are grouped together)
                // is only counted once.
                val groupedInDegree = node.getTransposedEdges.filter(edge ⇒ nodeIDs contains edge.targetID).length
                val groupedOutDegree = node.getEdges.filter(edge ⇒ nodeIDs contains edge.targetID).length
                if (groupedInDegree == 0 && groupedOutDegree > 0)
                    topLayerNodes = topLayerNodes + node
                else if (groupedInDegree > 0 && groupedOutDegree > 0)
                    middleLayerNodes = middleLayerNodes + node
                else if (groupedInDegree > 0 && groupedOutDegree == 0)
                    bottomLayerNodes = bottomLayerNodes + node
                else if (groupedInDegree == 0 && groupedOutDegree == 0)
                    sparatedNodes = sparatedNodes + node
            }

            var furtherLayers = true
            if (bottomLayerNodes.isEmpty && topLayerNodes.isEmpty) {
                furtherLayers = false
            }

            if (!sparatedNodes.isEmpty) {
                if (layer == 0)
                    sparatedNodes foreach { node ⇒
                        cluster.addNode(node)
                    }
                else
                    bottomLayerNodes = bottomLayerNodes ++ sparatedNodes
            }

            if (!bottomLayerNodes.isEmpty) {
                // create bottom layer
                createNewLayerClusterWithNodes(bottomLayerNodes)
            }

            if (!middleLayerNodes.isEmpty) {
                if (performRecursion && furtherLayers)
                    createLayers(middleLayerNodes)
                else
                    // create middle layer
                    createNewLayerClusterWithNodes(middleLayerNodes)
            }

            if (!topLayerNodes.isEmpty) {
                // create top layer
                createNewLayerClusterWithNodes(topLayerNodes)
            }
        }

        val inputNodes = cluster.getNodes.toSet
        cluster.clearNodes()
        createLayers(inputNodes)

        cluster
    }
}

object LayerClustering {

    def apply(performRecursion: Boolean = false): LayerClustering =
        new LayerClustering(performRecursion)

}
