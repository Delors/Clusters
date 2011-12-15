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
import de.tud.cs.st.bat.resolved.dependency.DependencyType._

/**
 * @author Thomas Schlosser
 *
 */
class Cluster(val uniqueID: Int, val identifier: String, val isRootCluster: Boolean) extends Node {

    val nodeMap = Map[Int, Node]()

    def this(uniqueID: Int, identifier: String) {
        this(uniqueID, identifier, false)
    }

    def addNode(node: Node) {
        nodeMap.put(node.uniqueID, node)
        if (node.parent != null) {
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

    def addEdge(src: Node, trgt: Node, dType: DependencyType) {
        sys.error("method \"addEdge\" is currently not supported in Cluster")
    }

    def getEdges: List[Edge] = {
        var edges = List[Edge]()
        nodeMap.values foreach {
            node ⇒
                node.getEdges foreach {
                    edge ⇒
                        val containsSource = nodeMap.contains(edge.source.uniqueID)
                        val containsTarget = nodeMap.contains(edge.target.uniqueID)
                        if (containsSource && !containsTarget) {
                            edges = edge :: edges
                        }
                }
        }
        edges
    }

    def getTransposedEdges: List[Edge] = {
        var edges = List[Edge]()
        nodeMap.values foreach {
            node ⇒
                node.getEdges foreach {
                    edge ⇒
                        val containsSource = nodeMap.contains(edge.source.uniqueID)
                        val containsTarget = nodeMap.contains(edge.target.uniqueID)
                        if (!containsSource && containsTarget) {
                            edges = edge :: edges
                        }
                }
        }
        edges
    }

    def toDot(includeSingleNodes: Boolean = true, includeEdges: Boolean = true)(implicit nodeBuffer: StringBuffer = new StringBuffer(), edgeBuffer: StringBuffer = new StringBuffer()): String = {
        val subGraphBuffer = new StringBuffer()
        if (isRootCluster) {
            nodeBuffer.append("digraph G {\n")
        }
        else {
            subGraphBuffer.append("subgraph cluster_")
            subGraphBuffer.append(identifier.replace(".", "_"))
            subGraphBuffer.append('_')
            subGraphBuffer.append(uniqueID)
            subGraphBuffer.append(" {\n")
        }

        var emptyCluster = true
        // add nodes
        getNodes foreach {
            case c: Cluster ⇒
                if (emptyCluster) {
                    nodeBuffer.append(subGraphBuffer)
                    emptyCluster = false
                }
                c.toDot(includeSingleNodes, includeEdges)(nodeBuffer, edgeBuffer)
            case n: SourceElementNode ⇒
                if (includeSingleNodes && emptyCluster) {
                    nodeBuffer.append(subGraphBuffer)
                    emptyCluster = false
                }
                n.toDot(includeSingleNodes, includeEdges)(nodeBuffer, edgeBuffer)
        }

        if (emptyCluster) {
            nodeBuffer.append("\t\"")
            nodeBuffer.append(uniqueID)
            nodeBuffer.append("\"[shape=box, label=\""+identifier+"\"];\n")
        }
        else if (!isRootCluster) {
            nodeBuffer.append("\tnode [style=filled,fillcolor=white,color=black];\n")
            nodeBuffer.append("\tstyle=filled;\n")
            nodeBuffer.append("\tfillcolor=lightgrey;\n")
            nodeBuffer.append("\tcolor=black;\n")
            nodeBuffer.append("\tlabel = \"")
            nodeBuffer.append(identifier)
            nodeBuffer.append("\";\n")
            nodeBuffer.append("}\n")
        }
        else {
            nodeBuffer.append(edgeBuffer)
            nodeBuffer.append("}\n")
        }
        return nodeBuffer.toString
    }
}
