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

import scala.collection.mutable.Set
import framework.pipeline.ClusteringStage
import framework.structure.Cluster
import framework.structure.Node
import framework.structure.TypeNode
import framework.structure.SourceElementNode
import de.tud.cs.st.bat.resolved.dependency._

/**
 *
 * NOTE: This stage is only (usefully) applicable on the application cluster or any of its sub clusters.
 * Since clusters are ignored in the current implementation, please use this stage before the input of this stage has been clustered.
 *
 * @author Thomas Schlosser
 *
 */
class ImplementationTestingSeparatorStage(
    val algorithmConfig: ImplementationTestingSeparatorStageConfiguration)
        extends ClusteringStage {

    override def performClustering(cluster: Cluster): Boolean = {
        val directlyTestRelatedNodes = extractDirectlyTestRelatedNodes(cluster)

        var allTestRelatedNodes: List[Node] = Nil
        var testRelatedTypes: Set[Node] = Set()
        directlyTestRelatedNodes foreach {
            case tn: TypeNode ⇒ {
                val mostOuterType = getMostOuterType(tn)
                if (!testRelatedTypes.contains(mostOuterType)) {
                    allTestRelatedNodes = getClassRelatedNodes(mostOuterType) ::: allTestRelatedNodes
                    testRelatedTypes = testRelatedTypes + mostOuterType
                }
            }
            case fn: SourceElementNode ⇒ { // this case matches for FieldNodes and MethodNodes
                val testType = fn.getOutgoingEdges.find(edge ⇒ edge.dType == DependencyType.IS_INSTANCE_MEMBER_OF || edge.dType == DependencyType.IS_CLASS_MEMBER_OF)
                if (testType.isDefined) {
                    val typeNode = testType.get.target
                    val mostOuterType = getMostOuterType(typeNode)
                    if (!testRelatedTypes.contains(mostOuterType)) {
                        allTestRelatedNodes = getClassRelatedNodes(mostOuterType) ::: allTestRelatedNodes
                        testRelatedTypes = testRelatedTypes + mostOuterType
                    }
                }
            }
            case _ ⇒
            // nothing to do in this case; the node is a cluster which is not considered in this stage
        }

        if (allTestRelatedNodes.nonEmpty) {
            val inputNodes = cluster.nodes.toSet

            cluster.clearNodes()
            cluster.clusterable = false

            val implementationCluster = clusterManager.createCluster(algorithmConfig.implementationClusterIdentifier, this.stageName)
            val testingCluster = clusterManager.createCluster(algorithmConfig.testingClusterIdentifier, this.stageName)
            testingCluster.clusterable = !algorithmConfig.markTestClusterAsUnclusterable

            cluster.addNode(implementationCluster)
            cluster.addNode(testingCluster)

            val implementationRelatedNodes = inputNodes -- allTestRelatedNodes

            implementationRelatedNodes foreach {
                implementationCluster.addNode(_)
            }

            allTestRelatedNodes foreach {
                testingCluster.addNode(_)
            }

            true
        }
        else {
            false
        }
    }

    protected def getMostOuterType(typeNode: Node): Node = {
        typeNode.getOutgoingEdges.find(edge ⇒ edge.dType == DependencyType.IS_INNER_CLASS_OF) match {
            case Some(edge) ⇒
                getMostOuterType(edge.target) // return most outer type of the outer class
            case None ⇒ // given type is no inner type; return the type itself
                typeNode
        }
    }

    protected def extractDirectlyTestRelatedNodes(cluster: Cluster): List[Node] = {
        var result: List[Node] = Nil
        cluster.nodes foreach { node ⇒
            // a node with an identifier that starts with a test library package prefix is considered as directly test related
            if (algorithmConfig.testLibrariesPackagePrefixes.exists(prfx ⇒ node.identifier.startsWith(prfx))) {
                result = node :: result
            }
            else {
                // check whether the current node has a direct dependency to a test library
                node.getOwnEdges foreach { edge ⇒
                    if (algorithmConfig.testLibrariesPackagePrefixes.exists(prfx ⇒ edge.target.identifier.startsWith(prfx))) {
                        result = node :: result
                    }
                }
            }
        }
        result
    }

    protected def getClassRelatedNodes(typeNode: Node): List[Node] = {
        var result = List(typeNode)
        for (tEdge ← typeNode.getOwnTransposedEdges) {
            if (tEdge.dType == DependencyType.IS_INSTANCE_MEMBER_OF ||
                tEdge.dType == DependencyType.IS_CLASS_MEMBER_OF) {
                result = tEdge.target :: result
            }
            else if (tEdge.dType == DependencyType.IS_INNER_CLASS_OF) {
                result = tEdge.target :: result
                result = getClassRelatedNodes(tEdge.target) ::: result
            }
        }
        result
    }
}

trait ImplementationTestingSeparatorStageConfiguration {
    val implementationClusterIdentifier = "implemenation"
    val testingClusterIdentifier = "testing"

    val testLibrariesPackagePrefixes = List("org.junit.", "org.scalatest.")
    val markTestClusterAsUnclusterable = true
}

object ImplementationTestingSeparatorStageConfiguration extends ImplementationTestingSeparatorStageConfiguration