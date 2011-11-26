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
import de.tud.cs.st.bat.resolved.ClassFile
import de.tud.cs.st.bat.resolved.Field_Info
import de.tud.cs.st.bat.resolved.Method_Info

/**
 * @author Thomas Schlosser
 *
 */
class SourceElementNode(val uniqueID: Int, val identifier: String) extends Node {

  protected var edges = List.empty[Edge]
  protected var transposedEdges = List.empty[Edge]

  def addEdge(src: Node, trgt: Node, dType: DependencyType) {
    if (src == this) {
      edges :+= new Edge(src, trgt, dType)
    }
    if (trgt == this) {
      transposedEdges :+= new Edge(trgt, src, dType)
    }
  }

  def getEdges(): List[Edge] =
    edges

  def getTransposedEdges(): List[Edge] =
    transposedEdges

  def toDot(implicit nodeBuffer: StringBuffer = new StringBuffer, edgeBuffer: StringBuffer = new StringBuffer): String = {
    nodeBuffer.append("\t\"")
    nodeBuffer.append(identifier)
    nodeBuffer.append("\";\n")

    // add egdes
    for (e <- getEdges()) {
      edgeBuffer.append("\t\"")
      edgeBuffer.append(e.source.identifier)
      edgeBuffer.append("\" -> \"")
      edgeBuffer.append(e.target.identifier)
      edgeBuffer.append("\"[label=\"")
      edgeBuffer.append(e.dType.toString)
      edgeBuffer.append("\"];\n")
    }
    nodeBuffer.toString
  }
}

case class ClassNode(id: Int, identif: String, clazz: ClassFile) extends SourceElementNode(id, identif) {

}

case class FieldNode(id: Int, identif: String, field: Field_Info) extends SourceElementNode(id, identif) {

}

case class MethodNode(id: Int, identif: String, method: Method_Info) extends SourceElementNode(id, identif) {

}