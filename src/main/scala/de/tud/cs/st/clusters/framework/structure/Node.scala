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

/**
 * @author Thomas Schlosser
 *
 */
trait Node {

    def identifier: String

    def uniqueID: Int

    var parent: Node = _

    protected var edges: List[Edge] = Nil
    //    protected val edgesMap: Map[(Int, DependencyType), Int] = Map()
    protected val edgesMap: Map[(Int, Object), Edge] = Map()
    protected var transposedEdges: List[Edge] = Nil

    def addEdge(srcID: Int, trgtID: Int, dType: DependencyType, count: Int = 1) {
        if (srcID == this.uniqueID) {
            val key = (trgtID, dType)
            var edge = edgesMap.getOrElse(key, null)
            if (edge == null) {
                edge = new Edge(srcID, trgtID, dType, 0)
                edges = edge :: edges
                edgesMap(key) = edge
            }
            edge.count += count
        }
        else if (trgtID == this.uniqueID) {
            transposedEdges = new Edge(trgtID, srcID, dType) :: transposedEdges
        }
    }

    def clearEdges() {
        //TODO: transposed edges of the target must be cleared too.
        edges = Nil
        edgesMap.clear()
        transposedEdges = Nil
    }

    def getEdges: List[Edge] =
        edges

    def getTransposedEdges: List[Edge] =
        transposedEdges

}