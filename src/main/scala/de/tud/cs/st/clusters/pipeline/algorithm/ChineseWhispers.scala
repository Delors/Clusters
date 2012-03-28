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
import scala.collection.mutable.Set
import framework.pipeline.ClusteringAlgorithm
import framework.structure.Cluster
import framework.structure.Node
import framework.structure.TypeNode
import framework.structure.FieldNode
import framework.structure.MethodNode
import framework.structure.util.ClusterManager
import de.tud.cs.st.bat.resolved.dependency._

/**
 *
 * @author Thomas Schlosser
 *
 */
class ChineseWhispers(
    val config: ChineseWhispersConfiguration)
        extends ClusteringAlgorithm {

    protected def doPerformClustering(cluster: Cluster): Boolean = {
        var createdNewCluster = false
        val inputChildren = cluster.children.toArray

        def calcClusters(sortedEdges: List[((Int, Int), Long)], nodes: Iterable[Node]): Array[Node] = {
            val clusters = Map[Int, ChineseWhispersNode]()
            for (node ← nodes) {
                val set = scala.collection.mutable.Set.empty[ChineseWhispersNode]
                val n = new ChineseWhispersNode(node.uniqueID, set)
                set += n
                n.cluster = set
                clusters(n.id) = n
            }

            val clusteredNodes = Set[Int]()

            for (((src, trg), wgt) ← sortedEdges) {
                if (!clusteredNodes.contains(src) || !clusteredNodes.contains(trg)) {
                    var trgSet = clusters(trg).cluster
                    var srcSet = clusters(src).cluster
                    trgSet ++= srcSet
                    for (n ← srcSet) {
                        n.cluster = trgSet
                    }
                    clusteredNodes.add(src)
                    clusteredNodes.add(trg)
                }
            }

            // TODO check what is the reason for OutOfMemoryError when using the following line
            // val clusterset = for (c ← clusters.values) yield { for (n ← c.cluster) yield n.id }
            var clusterset = Set[Set[Int]]()
            for (c ← clusters.values) {
                var cl = Set[Int]()
                for (n ← c.cluster) {
                    cl += n.id
                }
                clusterset += cl
            }

            val result = new Array[Node](clusterset.size)
            var i = 0
            clusterset foreach { nodes ⇒
                if (nodes.size > 1) {
                    val cluster = clusterManager.createCluster(config.clusterIdentifierPrefix + i, this.stageName, true)
                    createdNewCluster = true
                    nodes foreach { node ⇒
                        cluster.addChild(clusterManager.getNode(node))
                    }
                    result(i) = cluster
                }
                else {
                    result(i) = clusterManager.getNode(nodes.head)
                }
                i += 1
            }
            result
        }

        val weightMatrix: Map[(Int, Int), Long] = Map()

        cluster.edgesBetweenConnectedChildren foreach { edge ⇒
            val key = (edge.source.uniqueID, edge.target.uniqueID)
            val oldWeight: Long = weightMatrix.getOrElse(key, 0)
            val newWeight = oldWeight + (config.getWeight(edge.dType) * edge.count)
            if (newWeight != 0)
                weightMatrix(key) = newWeight
        }

        val sortedEdges = weightMatrix.toList.sortWith((a, b) ⇒ a._2 > b._2 || (a._2 == b._2 && (a._1._1 > b._1._1 || (a._1._1 == b._1._1 && a._1._2 > b._1._2))))

        cluster.clearChildren()

        calcClusters(sortedEdges, inputChildren) foreach {
            cluster.addChild(_)
        }

        cluster.clusterable = createdNewCluster
        createdNewCluster
    }

    private class ChineseWhispersNode(val id: Int, var cluster: scala.collection.mutable.Set[ChineseWhispersNode]) {
        override def toString: String = String.valueOf(id)
    }
}

trait ChineseWhispersConfiguration {
    val clusterIdentifierPrefix = "cw_"

    def getWeight(dType: DependencyType): Long = {
        // TODO: check which configuration creates the best results...
        dType match {
            // class/method/field definition related dependency types
            case DependencyType.EXTENDS                            ⇒ return 150
            case DependencyType.IMPLEMENTS                         ⇒ return 100
            case DependencyType.IS_INSTANCE_MEMBER_OF              ⇒ return 75
            case DependencyType.IS_CLASS_MEMBER_OF                 ⇒ return 75
            case DependencyType.IS_INNER_CLASS_OF                  ⇒ return 75

            // field definition related dependency types
            case DependencyType.IS_OF_TYPE                         ⇒ return 50
            case DependencyType.USES_CONSTANT_VALUE_OF_TYPE        ⇒

            // method definition related dependency types
            case DependencyType.RETURNS                            ⇒ return 35
            case DependencyType.HAS_PARAMETER_OF_TYPE              ⇒ return 50
            case DependencyType.THROWS                             ⇒
            case DependencyType.CATCHES                            ⇒

            // code related dependency types
            case DependencyType.HAS_LOCAL_VARIABLE_OF_TYPE         ⇒
            case DependencyType.CREATES_ARRAY_OF_TYPE              ⇒ return 30
            case DependencyType.CASTS_INTO                         ⇒ return 50
            case DependencyType.CHECKS_INSTANCEOF                  ⇒
            case DependencyType.CREATES                            ⇒ return 30
            case DependencyType.USES_FIELD_DECLARING_TYPE          ⇒
            case DependencyType.READS_FIELD                        ⇒ return 150
            case DependencyType.WRITES_FIELD                       ⇒ return 150
            case DependencyType.USES_FIELD_READ_TYPE               ⇒
            case DependencyType.USES_FIELD_WRITE_TYPE              ⇒
            case DependencyType.USES_PARAMETER_TYPE                ⇒
            case DependencyType.USES_RETURN_TYPE                   ⇒
            case DependencyType.USES_METHOD_DECLARING_TYPE         ⇒
            case DependencyType.CALLS_METHOD                       ⇒ return 90 //return 50
            case DependencyType.CALLS_INTERFACE_METHOD             ⇒ return 80 //return 40

            // annotation related dependency types
            case DependencyType.ANNOTATED_WITH                     ⇒
            case DependencyType.PARAMETER_ANNOTATED_WITH           ⇒

            // element value related dependency type
            case DependencyType.USES_DEFAULT_CLASS_VALUE_TYPE      ⇒
            case DependencyType.USES_DEFAULT_ENUM_VALUE_TYPE       ⇒
            case DependencyType.USES_ENUM_VALUE                    ⇒
            case DependencyType.USES_DEFAULT_ANNOTATION_VALUE_TYPE ⇒

            // signature/type parameter related dependency types
            case DependencyType.USES_TYPE_IN_TYPE_PARAMETERS       ⇒ return 15
        }
        0
    }
}

object ChineseWhispersConfiguration extends ChineseWhispersConfiguration
