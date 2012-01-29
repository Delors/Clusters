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

import scala.collection.mutable.Map
import framework.pipeline.ClusteringAlgorithm
import framework.pipeline.ClusteringAlgorithmConfiguration
import framework.pipeline.SameNeighborClusteringStage
import framework.pipeline.SameNeighborClusteringAlgorithmConfiguration
import framework.structure.Cluster
import framework.structure.Node
import framework.structure.Edge
import framework.structure.util.ClusterManager
import de.tud.cs.st.bat.resolved.dependency._

/**
 * @author Thomas Schlosser
 *
 */
trait ClassClusteringStage[C <: ClassClusteringAlgorithmConfiguration] extends ClusteringAlgorithm[C] {

}

trait ClassClusteringAlgorithmConfiguration extends ClusteringAlgorithmConfiguration {

}

class InternalClassClusteringStage(
    val algorithmConfig: InternalClassClusteringAlgorithmConfiguration)
        extends ClassClusteringStage[InternalClassClusteringAlgorithmConfiguration]
        with SameNeighborClusteringStage[InternalClassClusteringAlgorithmConfiguration] {

    override protected def isOfConsideredDependencyType(dType: DependencyType): Boolean =
        dType == DependencyType.IS_INSTANCE_MEMBER_OF || dType == DependencyType.IS_CLASS_MEMBER_OF

}

trait InternalClassClusteringAlgorithmConfiguration
        extends ClassClusteringAlgorithmConfiguration
        with SameNeighborClusteringAlgorithmConfiguration {

}

class ExternalClassClusteringStage(
    val algorithmConfig: ExternalClassClusteringAlgorithmConfiguration)
        extends ClassClusteringStage[ExternalClassClusteringAlgorithmConfiguration] {

    override def performClustering(cluster: Cluster): Cluster = {
        val classClustersMap = Map[Int, Set[Node]]()

        for (node ← cluster.getNodes) {
            //TODO implement based on nodes' identifiers 
            //            getClassMemberEdge(node) match {
            //                case Some(edge) ⇒
            //                    val classNodeID = edge.targetID
            //                    val clusterSet = classClustersMap.getOrElse(classNodeID, Set())
            //                    classClustersMap(classNodeID) = clusterSet + node
            //                case None ⇒
            //            }
        }

        cluster.clearNodes()
        for ((classNodeID, nodeSet) ← classClustersMap) {
            val classNode = clusterManager.getNode(classNodeID)
            val classCluster = clusterManager.createCluster(classNode.identifier, this.stageName)
            classCluster.addNode(classNode)
            nodeSet foreach {
                classCluster.addNode(_)
            }
            cluster.addNode(classCluster)
        }

        cluster
    }
}

trait ExternalClassClusteringAlgorithmConfiguration extends ClassClusteringAlgorithmConfiguration {

}
