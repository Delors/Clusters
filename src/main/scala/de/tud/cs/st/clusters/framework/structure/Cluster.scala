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

import de.tud.cs.st.bat.resolved.DependencyType._
import de.tud.cs.st.bat.resolved.dependency.DepBuilder

/**
 * @author Thomas Schlosser
 *
 */
class Cluster(val uniqueID: Int, val identifier: String, val isRootCluster: Boolean) extends Node {

  var nodes = List.empty[Node]

  def this() {
    //TODO: change
    this(-42, "Unnamed Cluster", false)
  }

  def this(identifier: String) {
    //TODO: change
    this(-42, identifier, false)
  }

  def this(uniqueID: Int, identifier: String) {
    this(uniqueID, identifier, false)
  }

  def addEdge(src: Node, trgt: Node, dType: DependencyType) {
    error("method \"addEdge\" is currently not supported in Cluster")
  }

  def getEdges(): List[Edge] = {
    error("method \"getEdges\" is currently not supported in Cluster")
  }

  def getTransposedEdges(): List[Edge] = {
    error("method \"getTransposedEdges\" is currently not supported in Cluster")
  }

  def toDot(implicit nodeBuffer: StringBuffer = new StringBuffer(), edgeBuffer: StringBuffer = new StringBuffer()): String = {
    if (isRootCluster) {
      nodeBuffer.append("digraph G {\n")
    } else {
      nodeBuffer.append("subgraph cluster_")
      nodeBuffer.append(identifier.replace(".", "_"))
      nodeBuffer.append(" {\n")
    }

    // add nodes
    for (node <- nodes) {
      node.toDot(nodeBuffer, edgeBuffer)
    }

    if (!isRootCluster) {
      nodeBuffer.append("\tnode [style=filled,fillcolor=white,color=black];\n")
      nodeBuffer.append("\tstyle=filled;\n")
      nodeBuffer.append("\tfillcolor=lightgrey;\n")
      nodeBuffer.append("\tcolor=black;\n")
      nodeBuffer.append("\tlabel = \"")
      nodeBuffer.append(identifier)
      nodeBuffer.append("\";\n")
    } else {
      nodeBuffer.append(edgeBuffer)
    }
    nodeBuffer.append("}\n")
    return nodeBuffer.toString
  }
}