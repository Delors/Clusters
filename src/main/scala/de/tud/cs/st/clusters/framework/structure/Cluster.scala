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
package framework
package structure

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import de.tud.cs.st.bat.resolved.dependency._

/**
 * @author Thomas Schlosser
 *
 */
class Cluster(
    val uniqueID: Int,
    val identifier: String,
    val isRootCluster: Boolean)
        extends Node {

    var clusterable = true

    val nodeMap = Map[Int, Node]()

    def this(uniqueID: Int, identifier: String) {
        this(uniqueID, identifier, false)
    }

    def addNode(node: Node) {
        nodeMap.put(node.uniqueID, node)
        if (node.parent != null && node.parent != this) {
            node.parent match {
                case c: Cluster ⇒ c.removeNode(node.uniqueID)
            }
        }
        node.parent = this
    }

    def removeNode(id: Int) {
        val removedNode = nodeMap.remove(id)
        if (removedNode.isDefined && removedNode.get.parent == this)
            removedNode.get.parent = null
    }

    def clearNodes() =
        nodeMap.clear

    def getNode(id: Int): Node = {
        nodeMap.getOrElse(id, sys.error("Node with ID["+id+"] was not found"))
    }

    def getNodes: Iterable[Node] = {
        nodeMap.values
    }

    def numberOfNodes: Int =
        nodeMap.size

    //TODO remove if not needed
    //    override def getEdges: List[Edge] = {
    //        var edges = super.getEdges
    //        if (!edges.isEmpty)
    //            return edges
    //
    //        // fetch edges from cluster elements
    //        edges = List[Edge]()
    //        nodeMap.values foreach {
    //            node ⇒
    //                node.getEdges foreach {
    //                    edge ⇒
    //                        val containsSource = nodeMap.contains(edge.sourceID)
    //                        val containsTarget = nodeMap.contains(edge.targetID)
    //                        if (containsSource && !containsTarget) {
    //                            edges = edge :: edges
    //                        }
    //                }
    //        }
    //        edges
    //    }

    //    override def getTransposedEdges: List[Edge] = {
    //        var edges = super.getTransposedEdges
    //        if (!edges.isEmpty)
    //            return edges
    //
    //        // fetch transposed edges from cluster elements
    //        edges = List[Edge]()
    //        nodeMap.values foreach {
    //            node ⇒
    //                node.getEdges foreach {
    //                    edge ⇒
    //                        val containsSource = nodeMap.contains(edge.sourceID)
    //                        val containsTarget = nodeMap.contains(edge.targetID)
    //                        if (!containsSource && containsTarget) {
    //                            edges = edge :: edges
    //                        }
    //                }
    //        }
    //        edges
    //    }

}
