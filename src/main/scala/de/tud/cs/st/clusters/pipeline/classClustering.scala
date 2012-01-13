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
import framework.pipeline.SameNeighborClustering
import framework.structure.Cluster
import framework.structure.Node
import framework.structure.Edge
import framework.structure.util.ClusterManager
import de.tud.cs.st.bat.resolved.dependency._

/**
 * @author Thomas Schlosser
 *
 */
trait ClassClustering extends Clustering {

}

class InternalClassClustering extends ClassClustering with SameNeighborClustering {

    override protected def isOfConsideredDependencyType(dType: DependencyType): Boolean =
        dType == DependencyType.IS_INSTANCE_MEMBER_OF || dType == DependencyType.IS_CLASS_MEMBER_OF

    //TODO: check whether some edges should be filtered here...
    //    override val edgeFilter: Int ⇒ Edge ⇒ Boolean = classNodeID ⇒ {
    //        case Edge(_, targetID, dType) ⇒
    //            targetID == classNodeID
    //        //                                dType == IS_INSTANCE_MEMBER_OF && targetID == classNodeID
    //    }
}

object InternalClassClustering {

    def apply(): InternalClassClustering = new InternalClassClustering()

}

class ExternalClassClustering extends ClassClustering {

    protected override def process(cluster: Cluster): Cluster = {
        val result = clusterManager.createCopy(cluster)

        val classClustersMap = Map[Int, Set[Node]]()

        for (node ← cluster.getNodes) {
            //TODO implement based on nodes' identifiers 
            //            getClassMemberEdge(node) match {
            //                case Some(edge) ⇒
            //                    val copy = NodeCloner.createDeepCopy(node)
            //                    val classNodeID = edge.targetID
            //                    val clusterSet = classClustersMap.getOrElse(classNodeID, Set())
            //                    classClustersMap(classNodeID) = clusterSet + copy
            //                case None ⇒
            //            }
        }

        var newClusters = Set[Cluster]()
        for ((classNodeID, nodeSet) ← classClustersMap) {
            val classNode = clusterManager.getNode(classNodeID)
            val classCluster = clusterManager.createCluster(classNode.identifier)
            nodeSet foreach {
                classCluster.addNode(_) // node was cloned before it was put into map
            }
            result.addNode(classCluster)
            newClusters = newClusters + classCluster
        }

        cluster
    }
}

object ExternalClassClustering {

    def apply(): ExternalClassClustering = new ExternalClassClustering()

}
