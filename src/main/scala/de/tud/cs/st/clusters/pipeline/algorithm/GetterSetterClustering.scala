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
import framework.structure.Cluster
import framework.structure.Node
import framework.structure.FieldNode
import framework.structure.MethodNode
import de.tud.cs.st.bat.resolved.dependency.DependencyType._
import de.tud.cs.st.bat.resolved.Field

/**
 * //allow more than one getter/setter...some inner classes define the same getter- setter-method as their outer class.
 *
 * // setter:
 * // WRITES_FIELD
 * // only param of field's type
 * // no return value
 * // check whether the name can be build by concatenating a setterPrefix(configurable in the getter-setter clustering stage configuration) and the field's name
 *
 * @author Thomas Schlosser
 *
 */
class GetterSetterClustering(
    val config: GetterSetterClusteringConfiguration)
        extends ClusteringAlgorithm {

    protected def doPerformClustering(cluster: Cluster): Boolean = {
        var createdNewCluster = false
        // TODO: cluster needs methods that return only type/field/method nodes => performance improvement
        val inputChildren = cluster.children.toArray
        for (node ← inputChildren) {
            node match {
                case FieldNode(_, _, Some(field)) ⇒
                    val optClusterBean = checkGetterSetterCluster(node, field)
                    optClusterBean match {
                        case Some(clusterBean) ⇒
                            // create setter/getter cluster
                            val gsCluster = clusterManager.createCluster(config.clusterIdentifierPrefix + clusterBean.field.identifier.toHRR, this.stageName)
                            gsCluster.addChild(clusterBean.field)
                            clusterBean.methods foreach { gsCluster.addChild(_) }
                            gsCluster.clusterable = false // a getter-setter cluster is treated as a primitive unit
                            cluster.addChild(gsCluster)
                            createdNewCluster = true
                        case None ⇒
                        // nothing to do if no getter-setter cluster was found
                    }
                case _ ⇒
                // nothing to do with non-field nodes
            }
        }
        createdNewCluster
    }

    private def checkGetterSetterCluster(node: Node, field: Field): Option[GetterSetterClusterBean] = {
        var gscBean = new GetterSetterClusterBean
        // use transposed edges to determine nodes that use this field
        var checkedNodes = Set[Int]()
        for (tEdge ← node.incomingEdges if (!checkedNodes.contains(tEdge.target.uniqueID))) {
            tEdge.dType match {
                case READS_FIELD ⇒
                    tEdge.target match {
                        case MethodNode(_, identifier, Some(method)) ⇒
                            if (!config.checkGetterNameMatching ||
                                config.getterPrefixes.exists(prefix ⇒ method.name.equalsIgnoreCase(prefix + field.name))) {
                                val descriptor = method.descriptor
                                if (descriptor.parameterTypes.isEmpty &&
                                    descriptor.returnType.equals(field.fieldType)) {
                                    gscBean.field = node
                                    gscBean.methods = tEdge.target :: gscBean.methods
                                }
                            }
                        case _ ⇒ // nothing to do if the target is no MethodNode 
                    }
                case WRITES_FIELD ⇒
                    tEdge.target match {
                        case MethodNode(_, identifier, Some(method)) ⇒
                            if (!config.checkSetterNameMatching ||
                                config.setterPrefixes.exists(prefix ⇒ method.name.equalsIgnoreCase(prefix + field.name))) {
                                val descriptor = method.descriptor
                                if (descriptor.parameterTypes.size == 1 && descriptor.parameterTypes(0).equals(field.fieldType) &&
                                    descriptor.returnType.isVoidType) {
                                    gscBean.field = node
                                    gscBean.methods = tEdge.target :: gscBean.methods
                                }
                            }
                        case _ ⇒ // nothing to do if the target is no MethodNode
                    }
                case _ ⇒ // nothing to do if the type is not a read or write access
            }
            checkedNodes += tEdge.target.uniqueID
        }

        if (gscBean.field == null) None else Some(gscBean)
    }

    private class GetterSetterClusterBean {
        var field: Node = _
        var methods: List[Node] = Nil
    }

}

trait GetterSetterClusteringConfiguration {
    val clusterIdentifierPrefix = "getter_setter_"

    val checkGetterNameMatching = true
    val checkSetterNameMatching = true
    val getterPrefixes: List[String] = List("get", "is")
    val setterPrefixes: List[String] = List("set")
}

object GetterSetterClusteringConfiguration extends GetterSetterClusteringConfiguration
