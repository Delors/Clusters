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

import de.tud.cs.st.bat.resolved.dependency.DependencyType._
import de.tud.cs.st.bat.resolved.ClassFile
import de.tud.cs.st.bat.resolved.Field
import de.tud.cs.st.bat.resolved.Method

/**
 * @author Thomas Schlosser
 *
 */
sealed trait SourceElementNode extends Node {

    val uniqueID: Int
    lazy val identifier = identifierFun()
    def identifierFun: () ⇒ String

    protected var edges: List[Edge] = Nil
    protected var transposedEdges: List[Edge] = Nil

    override def addEdge(srcID: Int, trgtID: Int, dType: DependencyType) {
        if (srcID == this.uniqueID) {
            edges = new Edge(srcID, trgtID, dType) :: edges
        }
        else if (trgtID == this.uniqueID) {
            transposedEdges = new Edge(trgtID, srcID, dType) :: transposedEdges
        }
    }

    def getEdges: List[Edge] =
        edges

    def getTransposedEdges: List[Edge] =
        transposedEdges

    def toDot(includeSingleNodes: Boolean = true, includeEdges: Boolean = true)(implicit nodeBuffer: StringBuffer = new StringBuffer, edgeBuffer: StringBuffer = new StringBuffer): String = {
        if (includeSingleNodes) {
            nodeBuffer.append("\t")
            nodeBuffer.append(uniqueID)
            nodeBuffer.append("[label=\"")
            nodeBuffer.append(identifier)
            nodeBuffer.append("\"];\n")

            // add egdes
            if (includeEdges)
                for (e ← getEdges) {
                    edgeBuffer.append("\t")
                    edgeBuffer.append(e.sourceID)
                    edgeBuffer.append(" -> ")
                    edgeBuffer.append(e.targetID)
                    edgeBuffer.append("[label=\"")
                    edgeBuffer.append(e.dType.toString)
                    edgeBuffer.append("\"];\n")
                }
        }
        nodeBuffer.toString
    }
}

case class TypeNode private (
    val uniqueID: Int,
    val identifierFun: () ⇒ String,
    var clazz: Option[ClassFile])
        extends SourceElementNode {

}

object TypeNode {
    def apply(id: Int, identifierFun: () ⇒ String, clazz: ClassFile): TypeNode =
        new TypeNode(id, identifierFun, Some(clazz))

    def apply(id: Int, identifierFun: () ⇒ String): TypeNode =
        new TypeNode(id, identifierFun, None)
}

case class FieldNode private (
    val uniqueID: Int,
    val identifierFun: () ⇒ String,
    var field: Option[Field])
        extends SourceElementNode {

}

object FieldNode {
    def apply(id: Int, identifierFun: () ⇒ String, field: Field): FieldNode =
        new FieldNode(id, identifierFun, Some(field))

    def apply(id: Int, identifierFun: () ⇒ String): FieldNode =
        new FieldNode(id, identifierFun, None)
}

case class MethodNode private (
    val uniqueID: Int,
    val identifierFun: () ⇒ String,
    var method: Option[Method])
        extends SourceElementNode {

}

object MethodNode {
    def apply(id: Int, identifierFun: () ⇒ String, method: Method): MethodNode =
        new MethodNode(id, identifierFun, Some(method))

    def apply(id: Int, identifierFun: () ⇒ String): MethodNode =
        new MethodNode(id, identifierFun, None)
}