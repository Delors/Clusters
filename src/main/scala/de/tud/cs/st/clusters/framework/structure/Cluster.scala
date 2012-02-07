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
    val isProjectCluster: Boolean) // TODO Why is this field required? Is a cluster not already identified as being a ProjectCluster by being the ProjectCluster in a pipeline's configuration?
        extends Node {

    // TODO
    def this(uniqueID: Int, identifier: String) {
        this(uniqueID, identifier, false)
    }

    override val isCluster: Boolean = true

    override var clusterable = true

    /////////////////////////////////////////////
    // children(nodes)-related stuff
    /////////////////////////////////////////////
    val nodeMap = Map[Int, Node]()

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

    /////////////////////////////////////////////
    // edges-related stuff
    /////////////////////////////////////////////
    override def getOutgoingEdges(): Set[Edge] = {
        // fetch edges from cluster elements
        var edges = Set[Edge]()
        nodeMap.values foreach {
            _.getOutgoingEdges foreach { edge ⇒
                val containsSource : Boolean = edge.source == this || edge.source.isChildOf(this.uniqueID)
                val containsTarget : Boolean = edge.target == this || edge.target.isChildOf(this.uniqueID)
                if (containsSource && !containsTarget) {
                    edges = edges + edge
                }
            }
        }
        // add own outgoing edges to the result
        edges ++ super.getOutgoingEdges()
    }

    override def getIncomingEdges(): Set[Edge] = {
        // fetch transposed edges from cluster elements
        var edges = Set[Edge]()
        nodeMap.values foreach {
            _.getIncomingEdges foreach { edge ⇒
                val containsSource = edge.source == this || edge.source.isChildOf(this.uniqueID)
                val containsTarget = edge.target == this || edge.target.isChildOf(this.uniqueID)
                if (!containsSource && containsTarget) {
                    edges = edges + edge
                }
            }
        }
        // add own incoming edges to the result
        edges ++ super.getIncomingEdges()
    }

    override def getInnerEdges(): Set[Edge] = {
        // fetch inner edges from cluster elements
        var edges = Set[Edge]()
        nodeMap.values foreach {
            _.getOutgoingEdges foreach { edge ⇒
                val containsSource = edge.source == this || edge.source.isChildOf(this.uniqueID)
                val containsTarget = edge.target == this || edge.target.isChildOf(this.uniqueID)
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

    override def getSpecialEdgesBetweenDirectChildren(): Set[Edge] = {
        var edges = Set[Edge]()
        nodeMap.values foreach {
            _.getOutgoingEdges foreach { edge ⇒
                val childrenContainsSource = edge.source.isChildOf(this.uniqueID)
                val childrenContainsTarget = edge.target.isChildOf(this.uniqueID)
                if (childrenContainsSource && childrenContainsTarget) {
                    edges = edges + new Edge(edge.source.getDirectChild(this.uniqueID), edge.target.getDirectChild(this.uniqueID), edge.dType, edge.count)
                }
            }
        }
        edges
    }

}
