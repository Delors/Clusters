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
import framework.pipeline.ClusteringStageConfiguration
import framework.structure.Cluster
import framework.structure.util.ClusterManager
import framework.structure.SourceElementNode
import framework.structure.Node

/**
 * @author Thomas Schlosser
 *
 */
trait EdgeGeneralizerStage[C <: EdgeGeneralizerStageConfiguration] extends ClusteringStage[C] {

    override def performClustering(cluster: Cluster): Cluster = {
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

trait EdgeGeneralizerStageConfiguration extends ClusteringStageConfiguration {

}

trait EdgeSourceGeneralizerStage extends EdgeGeneralizerStage[EdgeSourceGeneralizerStageConfiguration] {

    protected override def newSourceID(oldSourceID: Int): Int = {
        val oldNode = clusterManager.getNode(oldSourceID)
        if (oldNode != null
            && oldNode.parent != null
            && (configuration.considerOnlyUnclusterableSources
                || !oldNode.parent.clusterable)) {
            oldNode.parent.uniqueID
        }
        else {
            oldSourceID
        }
    }

}

trait EdgeSourceGeneralizerStageConfiguration extends EdgeGeneralizerStageConfiguration {
    val considerOnlyUnclusterableSources: Boolean
}

object EdgeSourceGeneralizerStage {

    def apply(c: EdgeSourceGeneralizerStageConfiguration): EdgeSourceGeneralizerStage =
        new { override val configuration = c } with EdgeSourceGeneralizerStage

}

trait EdgeTargetGeneralizerStage extends EdgeGeneralizerStage[EdgeTargetGeneralizerStageConfiguration] {

    protected override def newTargetID(oldTargetID: Int): Int = {
        val oldNode = clusterManager.getNode(oldTargetID)
        //if (oldNode != null && oldNode.parent != null) {
        if (oldNode != null
            && oldNode.parent != null
            && (!configuration.considerOnlyUnclusterableTargets
                || !oldNode.parent.clusterable)) {
            oldNode.parent.uniqueID
        }
        else {
            oldTargetID
        }
    }

}

trait EdgeTargetGeneralizerStageConfiguration extends EdgeGeneralizerStageConfiguration {
    val considerOnlyUnclusterableTargets: Boolean
}

object EdgeTargetGeneralizerStage {

    def apply(c: EdgeTargetGeneralizerStageConfiguration): EdgeTargetGeneralizerStage =
        new { override val configuration = c } with EdgeTargetGeneralizerStage

}

//TODO: how to fix this without generalization of EdgeSourceGeneralizerStage and EdgeTargetGeneralizerStage?
//trait AllEdgesGeneralizerStage
//        extends EdgeGeneralizerStage[AllEdgesGeneralizerStageConfiguration]
//        with EdgeSourceGeneralizerStage
//        with EdgeTargetGeneralizerStage {
//
//}

trait AllEdgesGeneralizerStageConfiguration
        extends EdgeGeneralizerStageConfiguration
        with EdgeSourceGeneralizerStageConfiguration
        with EdgeTargetGeneralizerStageConfiguration {
}

//object AllEdgesGeneralizerStage {
//
//    def apply(): AllEdgesGeneralizerStage =
//        new AllEdgesGeneralizerStage {
//            override val onlyUnclusterableSources = false
//            override val onlyUnclusterableTargets = false
//        }
//
//    def apply(
//        considerOnlyUnclusterableSources: Boolean,
//        considerOnlyUnclusterableTargets: Boolean): AllEdgesGeneralizerStage =
//        new AllEdgesGeneralizerStage {
//            override val onlyUnclusterableSources = considerOnlyUnclusterableSources
//            override val onlyUnclusterableTargets = considerOnlyUnclusterableTargets
//        }
//
//}
