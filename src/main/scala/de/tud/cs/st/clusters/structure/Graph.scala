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
package de.tud.cs.st.clusters.structure
import de.tud.cs.st.bat.resolved.DependencyType._
import de.tud.cs.st.bat.resolved.dependency.DepBuilder
import scala.collection.mutable.Map

/**
 * @author Thomas Schlosser
 *
 */
class Graph(val name: String) extends DepBuilder with DotableGraph {

  private var nodes = Array.empty[String]
  private var edges = Array.empty[AdjacenceListEdge]
  private var transposedEdges = Array.empty[AdjacenceListEdge]

  def this() {
    this(null)
  }

  def getID(identifier: String): Int = {
    var index = nodes.indexOf(identifier)
    if (index == -1) {
      nodes :+= identifier
      edges :+= null
      transposedEdges :+= null
      index = nodes.length - 1
    }
    index
  }

  def addDep(src: Int, trgt: Int, dType: DependencyType) {
    addEdge(src, trgt, dType)
  }

  def addEdge(src: Int, trgt: Int, eType: DependencyType) {
    def addEdge(start: Int, end: Int, adjLists: Array[AdjacenceListEdge]) {
      var successor = adjLists(start)
      val newEdge = new AdjacenceListEdge(end, eType)
      newEdge.successor = successor
      if (successor != null)
        successor.predecessor = newEdge
      adjLists(start) = newEdge
    }

    addEdge(src, trgt, edges)
    addEdge(trgt, src, transposedEdges)
  }

  def getEdges: Set[(Edge, Int)] = {
    var result = Map[Edge, Int]()
    for (i <- 0 to nodes.length - 1) {
      var e = edges(i)
      while (e != null) {
        val edge = (i, e.target, e.eType)
        result(edge) = result.getOrElse(edge, 0) + 1
        e = e.successor
      }
    }
    result.toSet
  }

  def getEdges(src: Int): AdjacenceListEdge = {
    edges(src)
  }

  def getNode(id: Int): Node =
    nodes(id)

  def size: Int =
    nodes.size
}