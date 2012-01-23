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

    override val isCluster: Boolean = true

    var clusterable = true

    val nodeMap = Map[Int, Node]()

    def this(uniqueID: Int, identifier: String) {
        this(uniqueID, identifier, false)
    }

    override def addNode(node: Node) {
        nodeMap.put(node.uniqueID, node)
        if (node.parent != null && node.parent != this) {
            node.parent match {
                case c: Cluster ⇒ c.removeNode(node.uniqueID)
            }
        }
        node.parent = this
    }

    override def removeNode(id: Int) {
        val removedNode = nodeMap.remove(id)
        if (removedNode.isDefined && removedNode.get.parent == this)
            removedNode.get.parent = null
    }

    override def clearNodes() =
        nodeMap.clear

    override def containsNode(id: Int): Boolean =
        nodeMap.contains(id) || nodeMap.values.exists(n ⇒ n.containsNode(id))

    override def getNode(id: Int): Node = {
        nodeMap.getOrElse(id, sys.error("Node with ID["+id+"] was not found"))
    }

    override def getNodes: Iterable[Node] = {
        nodeMap.values
    }

    override def numberOfNodes: Int =
        nodeMap.size

    //TODO: re-check these new methods...
    override def getOutgoingEdges(): Set[Edge] = {
        // fetch edges from cluster elements
        var edges = Set[Edge]()
        nodeMap.values foreach {
            _.getOutgoingEdges foreach { edge ⇒
                val containsSource = containsNode(edge.sourceID)
                val containsTarget = containsNode(edge.targetID)
                if (containsSource && !containsTarget) {
                    edges = edges + edge
                }
            }
        }
        edges ++ super.getOutgoingEdges()
    }

    override def getIncomingEdges(): Set[Edge] = {
        // fetch transposed edges from cluster elements
        var edges = Set[Edge]()
        nodeMap.values foreach {
            _.getIncomingEdges foreach { edge ⇒
                val containsSource = containsNode(edge.sourceID)
                val containsTarget = containsNode(edge.targetID)
                if (!containsSource && containsTarget) {
                    edges = edges + edge
                }
            }
        }
        edges ++ super.getIncomingEdges()
    }

    override def getInnerEdges(): Set[Edge] = {
        // fetch inner edges from cluster elements
        var edges = Set[Edge]()
        nodeMap.values foreach {
            _.getOutgoingEdges foreach { edge ⇒
                val containsSource = containsNode(edge.sourceID)
                val containsTarget = containsNode(edge.targetID)
                if (containsSource && containsTarget) {
                    edges = edges + edge
                }
            }
        }
        edges ++ super.getInnerEdges()
    }

    override def getAllEdges(): Set[Edge] =
        // fetch all edges from cluster elements and from cluster itself
        super.getAllEdges() ++ { for (node ← nodeMap.values; edge ← node.getAllEdges) yield edge }

}
