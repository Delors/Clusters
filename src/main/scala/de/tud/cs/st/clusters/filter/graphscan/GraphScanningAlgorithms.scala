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
package filter.graphscan
import structure.Node
import structure.Cluster
import structure.Edge
import scala.collection.mutable.Map

/**
 * @author Thomas Schlosser
 *
 */
object GraphScanningAlgorithms {
  /**
   * Represents state of an unvisited node.
   */
  private val WHITE = 0

  /**
   * Represents state of a visited but not completely finished node.
   */
  private val BLUE = 1

  /**
   * Represents state of a visited and completely finished node.
   */
  private val RED = 2

  def graphScanComplete(cluster: Cluster,
    startNode: Integer, dfs: Boolean, order: Array[Int])(implicit useTransposedEdges: Boolean = false): GraphScanResultBean = {
    // number of started searches
    var countStarts = 0
    implicit var resultBean = new GraphScanResultBean()
    // start first run
    graphScanIntern(cluster, {
      if (startNode == null) {
        if (order != null) order(0)
        else 0
      } else startNode
    }, dfs, 0, { countStarts += 1; countStarts - 1 })
    // run algorithm as long as not all nodes have been finished (all node colors!= white)
    for (i <- 0 to { if (order != null) order.length - 1 else cluster.nodes.length - 1 }) {
      val index = if (order != null) order(i) else cluster.nodes(i).uniqueID
      if (resultBean.color(index) == WHITE)
        graphScanIntern(cluster, index, dfs,
          resultBean.time, { countStarts += 1; countStarts - 1 })
    }
    resultBean
  }

  private def graphScanIntern(cluster: Cluster, startNode: Int,
    dfs: Boolean, discoveryTime: Int, colorOffset: Int)(implicit resultBean: GraphScanResultBean, useTransposedEdges: Boolean = false) {
    def getNextWhiteNode(edges: List[Edge]): Edge = {
      for (edge <- edges)
        if (resultBean.color(edge.target.uniqueID) == WHITE)
          return edge
      return null
    }

    def getNode(i: Int): Node = {
      for (n <- cluster.nodes)
        if (n.uniqueID == i)
          return n
      return null
    }

    // validate parameters
    if (startNode < 0 || startNode >= cluster.nodes.length)
      throw new IllegalArgumentException("Start node [" + startNode
        + "] is out of valid range {0,...,"
        + (cluster.nodes.length - 1) + "}")
    if (resultBean.color != null
      && resultBean.color(startNode) != WHITE)
      throw new IllegalArgumentException("Start node [" + startNode
        + "] with color[" + resultBean.color(startNode)
        + "] has already been visited.")

    // The node's color represents its state. 
    var q = new NodeSet(dfs)

    // INITIALIZATION:
    if (resultBean.color == null) {
      resultBean.color = Map()
      resultBean.discoveryTime = Map()
      resultBean.finishingTime = Map()
      resultBean.createOrderElements(cluster.nodes.length)
      resultBean.pi = Map()
      resultBean.evenDist = Map()
      for (i <- 0 to cluster.nodes.length - 1) {
        resultBean.color(i) = WHITE
        resultBean.discoveryTime(i) = 0
        resultBean.finishingTime(i) = -1
        resultBean.pi(i) = -1
      }
    }

    var t = discoveryTime
    resultBean.color(startNode) = BLUE
    resultBean.discoveryTime(startNode) = t
    resultBean.evenDist(startNode) = true
    t += 1
    q.add(startNode)

    // CALCULATION
    while (!q.isEmpty) {
      var u = q.getNext
      //graph.nodes(u) has to be replaced with hm... something :-)
      var current = getNextWhiteNode({ if (useTransposedEdges) getNode(u).getTransposedEdges() else getNode(u).getEdges() })
      if (current != null) {
        // found white neighbor node
        resultBean.color(current.target.uniqueID) = BLUE
        resultBean.evenDist(current.target.uniqueID) = !resultBean
          .evenDist(u)
        q.add(current.target.uniqueID)
        resultBean.discoveryTime(current.target.uniqueID) = t
        resultBean.pi(current.target.uniqueID) = u
      } else {
        // no white neighbor node was found
        resultBean.color(u) = RED + colorOffset
        resultBean.finishingTime(u) = t
        resultBean.order(resultBean.decreaseUnfinishedNodes()) = u
        q.remove // removes u
      }
      t += 1
    }
    // set the time at the end of the algorithm to the result bean
    resultBean.time = t
  }
}