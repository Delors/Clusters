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
import de.tud.cs.st.bat.resolved.StructureIdentifier

/**
 * @author Thomas Schlosser
 *
 */
class Cluster(
    val uniqueID: Int,
    val identifier: ClusterIdentifier)
        extends Node {

    override val isCluster: Boolean = true

    override var clusterable = true

    def isProjectCluster: Boolean =
        this.parent == null

    /////////////////////////////////////////////
    // children(nodes)-related stuff
    /////////////////////////////////////////////
    private val childrenMap = Map[Int, Node]()

    def children: Iterable[Node] =
        childrenMap.values

    def getChild(id: Int): Node =
        childrenMap.getOrElse(id, sys.error("Node with ID["+id+"] was not found"))

    def childCount: Int =
        childrenMap.size

    def addChild(child: Node) {
        childrenMap.put(child.uniqueID, child)
        if (child.parent != null && child.parent != this) {
            child.parent match {
                case c: Cluster ⇒ c.removeChild(child.uniqueID)
            }
        }
        child.parent = this
    }

    def removeChild(id: Int) {
        val removeChild = childrenMap.remove(id)
        if (removeChild.isDefined && removeChild.get.parent == this)
            removeChild.get.parent = null
    }

    def clearChildren() =
        childrenMap.clear

    def hasDescendant(id: Int): Boolean =
        childrenMap.contains(id) || childrenMap.values.exists(n ⇒ n.hasDescendant(id))

    /////////////////////////////////////////////
    // edges-related stuff
    /////////////////////////////////////////////
    override def outgoingEdges: Set[Edge] = {
        // fetch edges from cluster elements
        var edges = Set[Edge]()
        childrenMap.values foreach {
            _.outgoingEdges foreach { edge ⇒
                val containsTarget: Boolean = edge.target == this || edge.target.isDescendantOf(this.uniqueID)
                if (!containsTarget) {
                    edges = edges + edge
                }
            }
        }
        // add own outgoing edges to the result
        edges ++ super.outgoingEdges
    }

    override def incomingEdges: Set[Edge] = {
        // fetch transposed edges from cluster elements
        var edges = Set[Edge]()
        childrenMap.values foreach {
            _.incomingEdges foreach { edge ⇒
                val containsTarget = edge.target == this || edge.target.isDescendantOf(this.uniqueID)
                if (!containsTarget) {
                    edges = edges + edge
                }
            }
        }
        // add own incoming edges to the result
        edges ++ super.incomingEdges
    }

    override def edgesBetweenDescendants: Set[Edge] = {
        // fetch inner edges from cluster elements
        var edges = Set[Edge]()
        childrenMap.values foreach {
            _.outgoingEdges foreach { edge ⇒
                val containsTarget = edge.target == this || edge.target.isDescendantOf(this.uniqueID)
                if (containsTarget) {
                    edges = edges + edge
                }
            }
        }
        edges ++ super.edgesBetweenDescendants
    }

    override def allRelatedEdges: Set[Edge] =
        // fetch all edges from cluster elements and from cluster itself
        super.allRelatedEdges ++ { for (node ← childrenMap.values; edge ← node.allRelatedEdges) yield edge }

    def edgesBetweenConnectedChildren: Set[Edge] = {
        /**
         * Gets the child of the node that has the given pathTargetID as uniqueID
         * and that is member of the path between pathSource and the node with the pathTargetID.
         */
        def secondToLastOnPath(pathSource: Node, pathTargetID: Int): Node = {
            require(pathSource.parent != null, println("parent is null"))

            if (pathSource.parent.uniqueID == pathTargetID)
                return pathSource
            else
                return secondToLastOnPath(pathSource.parent, pathTargetID)
        }

        var edges = Set[Edge]()
        childrenMap.values foreach {
            _.outgoingEdges foreach { edge ⇒
                val childrenContainsTarget = edge.target.isDescendantOf(this.uniqueID)
                if (childrenContainsTarget) {
                    edges = edges + new Edge(secondToLastOnPath(edge.source, this.uniqueID), secondToLastOnPath(edge.target, this.uniqueID), edge.dType, edge.count)
                }
            }
        }
        edges
    }

    /////////////////////////////////////////////
    // clone-related stuff
    /////////////////////////////////////////////

    def cloneNode: Cluster = {
        val clone = new Cluster(this.uniqueID, this.identifier)
        clone.clusterable = this.clusterable
        clone.metaInfo ++= this.metaInfo
        // add clones of cluster elements to the cluster's clone
        this.children foreach { node ⇒
            val childClone = node.cloneNode
            clone.addChild(childClone)
        }
        clone
    }

}

case class ClusterIdentifier(name: String, val uniqueName: String) extends StructureIdentifier {

    def toHRR = name

    def declaringPackage = None
}
