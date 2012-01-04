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
import framework.pipeline.IntermediateClustering
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
class InternExternClustering(
    val builder: ClusterBuilder,
    val internClustering: Option[Clustering],
    val externClustering: Option[Clustering],
    val successorClustering: Option[Clustering],
    val newClusterClustering: Option[Clustering])
        extends IntermediateClustering {

    protected override def process(cluster: Cluster): Cluster = {
        val result = NodeCloner.createCopy(cluster)

        def clusterNewCluster(
            newCluster: Cluster,
            firstChoice: Option[Clustering],
            secondChoice: Option[Clustering]) {
            if (firstChoice.isDefined || secondChoice.isDefined) {
                val selectedClustering =
                    if (firstChoice.isDefined)
                        firstChoice.get
                    else
                        secondChoice.get
                val clusteredCluster = selectedClustering.process(Array(newCluster))
                result.removeNode(newCluster.uniqueID)
                result.addNode(clusteredCluster(0))
            }
        }

        //TODO change impl.
        // If there is any class file in a package, then all classes in that package are intern.
        // Otherwise, the class is an external one.
        val intern = builder.createCluster("intern")
        val extern = builder.createCluster("extern")
        result.addNode(intern)
        result.addNode(extern)
        for (node ← cluster.getNodes) {
            val copy = NodeCloner.createDeepCopy(node)
            node match {
                case TypeNode(_, _, Some(_)) ⇒
                    intern.addNode(copy)
                case TypeNode(_, _, None) ⇒
                    extern.addNode(copy)
                case FieldNode(_, _, Some(_)) ⇒
                    intern.addNode(copy)
                case FieldNode(_, _, None) ⇒
                    extern.addNode(copy)
                case MethodNode(_, _, Some(_)) ⇒
                    intern.addNode(copy)
                case MethodNode(_, _, None) ⇒
                    extern.addNode(copy)
                case _ ⇒
                    println("intern/extern is unknown")
                    result.addNode(copy)
            }
        }

        clusterNewCluster(intern, internClustering, newClusterClustering)
        clusterNewCluster(extern, externClustering, newClusterClustering)

        result
    }
}

object InternExternClustering {

    def apply(
        clusterBuilder: ClusterBuilder,
        internClustering: Clustering = null,
        externClustering: Clustering = null,
        successorClustering: Clustering = null): InternExternClustering =
        new InternExternClustering(
            clusterBuilder,
            if (internClustering == null) None else Some(internClustering),
            if (externClustering == null) None else Some(externClustering),
            if (successorClustering == null) None else Some(successorClustering),
            None)

}
