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

import de.tud.cs.st.bat.resolved.dependency._
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap

/**
 * @author Thomas Schlosser
 *
 */
trait Node {

    def identifier: String

    def uniqueID: Int

    val isCluster: Boolean = false

    var clusterable: Boolean

    val metaInfo: Map[String, String] = HashMap()

    /////////////////////////////////////////////
    // parent-related stuff
    /////////////////////////////////////////////
    var parent: Cluster = _

    def level: Int =
        if (parent != null) parent.level + 1 else 1

    def getNodeOfLevel(level: Int): Node =
        this.getAncestor(this.level - level)

    private def getAncestor(levelDiff: Int): Node =
        if (levelDiff == 0) {
            this
        } else {
            if (levelDiff > 0 && this.parent != null) {
                parent.getAncestor(levelDiff - 1)
            } else {
                sys.error("node[" + this.uniqueID + "] has no ancestor with levelDiff of \"" + levelDiff + "\"")
            }
        }

    def getDirectChild(parentID: Int): Node = {
        if (this.parent != null) { // TODO use "require"
            if (this.parent.uniqueID == parentID)
                return this
            else
                return parent.getDirectChild(parentID)
        } else {
            sys.error("")
        }
    }

    def isChildOf(parentID: Int): Boolean =
        this.parent != null && (
            this.parent.uniqueID == parentID ||
            this.parent.isChildOf(parentID))

    /////////////////////////////////////////////
    // edges-related stuff
    /////////////////////////////////////////////
    protected var edges: List[Edge] = Nil
    protected var transposedEdges: List[Edge] = Nil
    protected val edgesMap: Map[(Node, Object), Edge] = Map() // Map[(Int, DependencyType), Int]
    protected val transposedEdgesMap: Map[(Node, Object), Edge] = Map() // Map[(Int, DependencyType), Int]

    def addEdge(target: Node, dType: DependencyType, count: Int = 1, createTransposedEdge: Boolean = true) {
        val key = (target, dType)
        var edge = edgesMap.getOrElse(key, null)
        if (edge == null) {
            edge = new Edge(this, target, dType, 0)
            edges = edge :: edges
            edgesMap(key) = edge
        }
        edge.count += count
        if (createTransposedEdge)
            target.addTransposedEdge(this, dType, count, false)
    }

    private def addTransposedEdge(source: Node, dType: DependencyType, count: Int = 1, createEdge: Boolean = true) {
        val key = (source, dType)
        var tEdge = transposedEdgesMap.getOrElse(key, null)
        if (tEdge == null) {
            // Since this is a transposed edge, the source is considered as target
            tEdge = new Edge(this, source, dType, 0)
            transposedEdges = tEdge :: transposedEdges
            transposedEdgesMap(key) = tEdge
        }
        tEdge.count += count
        if (createEdge)
            source.addEdge(source, dType, count, false)
    }

    def clearEdges() {
        edges = Nil
        edgesMap.clear()
        transposedEdges = Nil
        transposedEdgesMap.clear()
    }

    /**
     * Gets all edges whose source is this node.
     */
    def getOwnEdges: List[Edge] =
        edges

    /**
     * Gets all transposed edges whose target is this node.
     */
    def getOwnTransposedEdges: List[Edge] =
        transposedEdges

    /**
     * Gets all edges whose source is this node or any of its children
     * and whose target is neither this node nor any of its children.
     */
    def getOutgoingEdges(): Set[Edge] = {
        edges.filter(e ⇒ !(e.target == this || e.target.isChildOf(this.uniqueID))).toSet
    }

    /**
     * Gets all transposed edges whose target is this node or any of its children
     * and whose source is neither this node nor any of its children.
     */
    def getIncomingEdges(): Set[Edge] = {
        transposedEdges.filter(e ⇒ !(e.source == this || e.source.isChildOf(this.uniqueID))).toSet
    }

    /**
     * Gets all edges whose source and target are children of this node.
     */
    def getInnerEdges(): Set[Edge] =
        Set()

    /**
     * Gets all edges that are somehow related to this node or its children.
     */
    def getAllEdges(): Set[Edge] = {
        getOutgoingEdges() ++ getIncomingEdges() ++ getInnerEdges()
    }

    /**
     * Gets all outgoing edges of nodes whose parent is this node and whose target
     * is also a child of this node. The target of the returned (special) edges is
     * set to the direct child -- on the path to the original target -- of this node.
     */
    def getSpecialEdgesBetweenDirectChildren(): Set[Edge] = {
        Set()
    }

    /////////////////////////////////////////////
    // children(nodes)-related stuff
    /////////////////////////////////////////////
    def addNode(node: Node) {
        // FIXME replace by throw new UnsupportedOperationException
        sys.error("This method call is not allowed on this kind of node!")
    }

    def removeNode(id: Int) {
        // FIXME replace by throw new UnsupportedOperationException
        sys.error("This method call is not allowed on this kind of node!")
    }

    def clearNodes() {
        // FIXME replace by throw new UnsupportedOperationException
        sys.error("This method call is not allowed on this kind of node!")
    }

    // TODO make the following abstract and implement these methods in Cluster and SourceElementNode instead

    def containsNode(id: Int): Boolean =
        false

    def getNode(id: Int): Node = {
        // FIXME replace by throw new UnsupportedOperationException
        sys.error("This method call is not allowed on this kind of node!")
    }

    def getNodes: Iterable[Node] = {
        Iterable()
    }

    // ISSUE Consider using "childCount" (if it applies)
    def numberOfNodes: Int =
        0

}