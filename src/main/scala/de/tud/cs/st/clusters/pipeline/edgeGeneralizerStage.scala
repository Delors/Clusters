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

import framework.pipeline.ClusteringStage
import framework.structure.Cluster
import framework.structure.util.ClusterManager
import framework.structure.SourceElementNode
import framework.structure.Node

/**
 * @author Thomas Schlosser
 *
 */
//FIXME fundamental problem with the ClusterBuilder concept for ID->Node lookups,
// because nodes have been cloned in each step and the nodes in ClusterBuilder represent
// the state of the root cluster (for each point in time)
trait EdgeGeneralizerStage extends ClusteringStage {

    protected override def process(cluster: Cluster): Cluster = {
        val inputCluster = clusterManager.createDeepCopy(cluster)
        cluster.clearEdges()
        removeAllOriginalEdges(cluster)
        convertCluster(inputCluster)
        clusterManager.getRootCluster
    }

    private def removeAllOriginalEdges(originalCluster: Cluster) {
        for (node ← originalCluster.getNodes) {
            node.clearEdges()
            node match {
                case c: Cluster ⇒
                    removeAllOriginalEdges(c)
                case _ ⇒ // nothing further to do in case of source element nodes
            }
        }
    }

    /**
     * Side effect: edges in the clusterManager's cluster that corresponds to the given cluster are generalized
     */
    protected def convertCluster(inputCluster: Cluster) {
        for (node ← inputCluster.getNodes) {
            convertEdges(node)
            node match {
                case c: Cluster ⇒
                    convertCluster(c)
                case _ ⇒ // nothing further to do in case of source element nodes
            }
        }
    }

    protected def convertEdges(node: Node) {
        for (edge ← node.getEdges) {
            val newSource = clusterManager.getNode(newSourceID(edge.sourceID))
            val newTarget = clusterManager.getNode(newTargetID(edge.targetID))
            newSource.addEdge(newSource.uniqueID, newTarget.uniqueID, edge.dType, edge.count)
            newTarget.addEdge(newSource.uniqueID, newTarget.uniqueID, edge.dType, edge.count)
        }
    }

    protected def newSourceID(oldSourceID: Int): Int =
        oldSourceID

    protected def newTargetID(oldTargetID: Int): Int =
        oldTargetID

}

class EdgeSourceGeneralizerStage(val considerOnlyUnclusterableSources: Boolean) extends EdgeGeneralizerStage {

    protected override def newSourceID(oldSourceID: Int): Int = {
        val oldNode = clusterManager.getNode(oldSourceID)
        if (oldNode != null
            && oldNode.parent != null
            && (considerOnlyUnclusterableSources
                || !oldNode.parent.clusterable)) {
            oldNode.parent.uniqueID
        }
        else {
            oldSourceID
        }
    }

}

object EdgeSourceGeneralizerStage {

    def apply(): EdgeSourceGeneralizerStage =
        new EdgeSourceGeneralizerStage(false)

    def apply(considerOnlyUnclusterableSources: Boolean): EdgeSourceGeneralizerStage =
        new EdgeSourceGeneralizerStage(considerOnlyUnclusterableSources)

}

class EdgeTargetGeneralizerStage(val considerOnlyUnclusterableTargets: Boolean) extends EdgeGeneralizerStage {

    protected override def newTargetID(oldTargetID: Int): Int = {
        val oldNode = clusterManager.getNode(oldTargetID)
        //if (oldNode != null && oldNode.parent != null) {
        if (oldNode != null
            && oldNode.parent != null
            && (!considerOnlyUnclusterableTargets
                || !oldNode.parent.clusterable)) {
            oldNode.parent.uniqueID
        }
        else {
            oldTargetID
        }
    }

}

object EdgeTargetGeneralizerStage {

    def apply(): EdgeTargetGeneralizerStage =
        new EdgeTargetGeneralizerStage(false)

    def apply(considerOnlyUnclusterableTargets: Boolean): EdgeTargetGeneralizerStage =
        new EdgeTargetGeneralizerStage(considerOnlyUnclusterableTargets)

}
