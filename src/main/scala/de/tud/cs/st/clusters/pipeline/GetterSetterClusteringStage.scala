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

import scala.collection.mutable.ListBuffer
import framework.pipeline.ClusteringStage
import framework.pipeline.ClusteringStageConfiguration
import framework.structure.Cluster
import framework.structure.Node
import framework.structure.FieldNode
import framework.structure.MethodNode
import framework.structure.util.ClusterManager
import de.tud.cs.st.bat.resolved.dependency.DependencyType._
import de.tud.cs.st.bat.resolved.Type
import de.tud.cs.st.bat.resolved.VoidType
import de.tud.cs.st.bat.resolved.Field

/**
 * @author Thomas Schlosser
 *
 */
trait GetterSetterClusteringStage extends ClusteringStage[GetterSetterClusteringStageConfiguration] {

    val getterPrefix: Option[String] = Some("get")
    val setterPrefix: Option[String] = Some("set")

    protected override def process(cluster: Cluster): Cluster = {
        println("GETTER_SETTER_CLUSTERING_STAGE # START")
        def checkGetterSetterCluster(node: Node, field: Field): Option[GetterSetterClusterBean] = {
            var gscBean = new GetterSetterClusterBean
            // use transposed edges to determine nodes that use this field
            //TODO: test and refine this algorithm with more complex classes...
            //add mechanism to algorithms that allows to specify/configure that a cluster is as fine granular as required
            var checkedNodes = Set[Int]()
            //TODO: edge count should be considered...have to be equal to one
            for (tEdge ← node.getTransposedEdges) {
                val target = clusterManager.getNode(tEdge.targetID)
                if (!checkedNodes.contains(tEdge.targetID)) {
                    tEdge.dType match {
                        case READS_FIELD ⇒
                            target match {
                                case MethodNode(_, identifier, Some(method)) ⇒
                                    if (getterPrefix == None || method.name.startsWith(getterPrefix.get)) {
                                        val descriptor = method.descriptor
                                        if (descriptor.parameterTypes.isEmpty &&
                                            descriptor.returnType.equals(field.fieldType)) {
                                            if (gscBean.getter != null)
                                                sys.error("only one getter is allowed: current["+gscBean.getter.identifier+"], new["+identifier()+"]")
                                            gscBean.field = node
                                            gscBean.getter = target
                                            gscBean.hasGetter = true
                                        }
                                    }
                                case _ ⇒ Nil
                            }
                        case WRITES_FIELD ⇒
                            target match {
                                case MethodNode(_, identifier, Some(method)) ⇒
                                    if (setterPrefix == None || method.name.startsWith(setterPrefix.get)) {
                                        val descriptor = method.descriptor
                                        if (descriptor.parameterTypes.size == 1 && descriptor.parameterTypes(0).equals(field.fieldType) &&
                                            descriptor.returnType.isVoidType) {
                                            if (gscBean.setter != null)
                                                sys.error("only one setter is allowed: current["+gscBean.setter.identifier+"], new["+identifier+"]")
                                            gscBean.field = node
                                            gscBean.setter = target
                                            gscBean.hasSetter = true
                                        }
                                    }
                                case _ ⇒ Nil
                            }
                        //          case a => println(a.toString) //return None //this node is out
                    }
                    checkedNodes += tEdge.targetID
                }
            }
            if (gscBean.field == null) None else Some(gscBean)
        }
        // TODO: cluster needs methods that return only type/field/method nodes => performance improvement
        val inputNodes = cluster.getNodes.toArray
        cluster.clearNodes()
        println(inputNodes.size)
        for (node ← inputNodes) {
            node match {
                case FieldNode(_, _, Some(field)) ⇒
                    val optClusterBean = checkGetterSetterCluster(node, field)
                    optClusterBean match {
                        case Some(clusterBean) ⇒
                            // create setter/getter cluster
                            println("GETTER_SETTER_CLUSTER")
                            val gsCluster = clusterManager.createCluster("Getter_Setter_"+clusterBean.field.identifier)
                            gsCluster.addNode(clusterBean.field)
                            if (clusterBean.hasGetter) {
                                gsCluster.addNode(clusterBean.getter)
                            }
                            if (clusterBean.hasSetter) {
                                gsCluster.addNode(clusterBean.setter)
                            }
                            gsCluster.clusterable = false
                            cluster.addNode(gsCluster)
                        case None ⇒
                            cluster.addNode(node)
                    }
                case _ ⇒
                    cluster.addNode(node)
            }
        }
        cluster
    }

    class GetterSetterClusterBean {
        var field: Node = _
        var getter: Node = _
        var setter: Node = _
        var hasSetter: Boolean = false
        var hasGetter: Boolean = false

        // setter
        // WRITES_FIELD
        // only param of field's type
        // no return value
        // use field write type evtl. has local variable of type(parameter)
        // uses field declaring type of the corresponding field
        // ignore "is instance member of"...
    }

}

trait GetterSetterClusteringStageConfiguration extends ClusteringStageConfiguration {

}

object GetterSetterClusteringStage {

    def apply(c: GetterSetterClusteringStageConfiguration): GetterSetterClusteringStage =
        new { override val configuration = c } with GetterSetterClusteringStage

}
