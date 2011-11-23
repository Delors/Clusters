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
package filter
import de.tud.cs.st.clusters.structure.Cluster
import java.io.File
import de.tud.cs.st.bat.resolved.DependencyType._
import structure.Node
import de.tud.cs.st.clusters.structure.FieldNode
import de.tud.cs.st.bat.resolved.Type
import de.tud.cs.st.bat.resolved.VoidType
import de.tud.cs.st.clusters.structure.MethodNode

/**
 * @author Thomas Schlosser
 *
 */
trait GetterSetterClustering extends ClusterFilter {

  val getterPrefix: Option[String] = Some("get")
  val setterPrefix: Option[String] = Some("set")

  abstract override def filter(clusters: Array[Cluster], projectRootDir: File): Array[Cluster] = {
    for (cluster <- clusters) {
      createGetterSetterClusters(cluster)
    }
    super.filter(clusters, projectRootDir)
    clusters
  }

  def createGetterSetterClusters(cluster: Cluster) {
    def checkGetterSetterCluster(node: FieldNode): Option[GetterSetterClusterBean] = {
      def getFieldType(fieldNode: Node): Option[Node] = {
        for (edge <- fieldNode.getEdges) {
          if (edge.dType.equals(IS_OF_TYPE))
            return Some(edge.target)
        }
        None
      }

      var gscBean = new GetterSetterClusterBean
      // use transposed edges to determine nodes that use this field
      //TODO: test and refine this algorithm with more complex classes...
      //add mechanism to algorithms that allows to specify/configure that a cluster is as fine granular as required
      //TODO: impl. mechanism that allows filter algorithms to create clusters with an unique ID
      var checkedNodes = Array.empty[Node]
      for (tEdge <- node.getTransposedEdges) {
        if (!checkedNodes.contains(tEdge.target)) {
          tEdge.dType match {
            case READS_FIELD =>
              tEdge.target match {
                case mn: MethodNode =>
                  if (getterPrefix == None || mn.method.name.startsWith(getterPrefix.get)) {
                    val descriptor = mn.method.descriptor
                    if (descriptor.parameterTypes.isEmpty &&
                      descriptor.returnType.equals(node.field.descriptor.fieldType)) {
                      if (gscBean.getter != null)
                        error("only one getter is allowed: current[" + gscBean.getter.identifier + "], new[" + mn.identifier + "]")
                      gscBean.field = node
                      gscBean.getter = mn
                      gscBean.hasGetter = true
                    }
                  }
                case _ => Nil
              }
            case WRITES_FIELD =>
              tEdge.target match {
                case mn: MethodNode =>
                  if (setterPrefix == None || mn.method.name.startsWith(setterPrefix.get)) {
                    val descriptor = mn.method.descriptor
                    if (descriptor.parameterTypes.size == 1 && descriptor.parameterTypes(0).equals(node.field.descriptor.fieldType) &&
                      descriptor.returnType.isVoidType) {
                      if (gscBean.setter != null)
                        error("only one setter is allowed: current[" + gscBean.setter.identifier + "], new[" + mn.identifier + "]")
                      gscBean.field = node
                      gscBean.setter = mn
                      gscBean.hasSetter = true
                    }
                  }
                case _ => Nil
              }
            //          case a => println(a.toString) //return None //this node is out
          }
          checkedNodes :+= tEdge.target
        }
      }
      if (gscBean.field == null) None else Some(gscBean)
    }
    // TODO: cluster needs methods that return only type/field/method nodes => performance improvement
    val nodes = cluster.nodes
    cluster.nodes = List()
    for (node <- nodes) {
      if (node.isInstanceOf[FieldNode]) {
        println(node.asInstanceOf[FieldNode])
        val optClusterBean = checkGetterSetterCluster(node.asInstanceOf[FieldNode])
        optClusterBean match {
          case Some(clusterBean) =>
            // create setter/getter cluster
            println("GETTER_SETTER_CLUSTER")
            val gsCluster = new Cluster("Getter_Setter_" + clusterBean.field.identifier)
            gsCluster.nodes :+= clusterBean.field
            if (clusterBean.hasGetter) {
              gsCluster.nodes :+= clusterBean.getter
            }
            if (clusterBean.hasSetter) {
              gsCluster.nodes :+= clusterBean.setter
            }
            cluster.nodes :+= gsCluster
          case None =>
            cluster.nodes :+= node
        }
      }
    }
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

  class GetterBean {
    val field: Node = null
    val method: Node = null
  }
}