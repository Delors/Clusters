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
import de.tud.cs.st.bat.resolved.ClassFile
import de.tud.cs.st.bat.resolved.Field
import de.tud.cs.st.bat.resolved.Method

/**
 * @author Thomas Schlosser
 *
 */
sealed trait SourceElementNode extends Node {

    def identifierFun: () ⇒ String
    override lazy val identifier = identifierFun()

    override val isCluster: Boolean = false

    override var clusterable = false

    /////////////////////////////////////////////
    // edges-related stuff
    /////////////////////////////////////////////
    def getSpecialEdgesBetweenDirectChildren(): Set[Edge] = {
        Set()
    }

    /////////////////////////////////////////////
    // children(nodes)-related stuff
    /////////////////////////////////////////////
    def addNode(node: Node) {
        throw new UnsupportedOperationException("This method call is not allowed on this kind of node!")
    }

    def removeNode(id: Int) {
        throw new UnsupportedOperationException("This method call is not allowed on this kind of node!")
    }

    def clearNodes() {
        throw new UnsupportedOperationException("This method call is not allowed on this kind of node!")
    }

    def containsNode(id: Int): Boolean =
        false

    def getNode(id: Int): Node = {
        throw new UnsupportedOperationException("This method call is not allowed on this kind of node!")
    }

    def nodes: Iterable[Node] = {
        Iterable()
    }

    def childCount: Int =
        0

}

case class TypeNode(
    val uniqueID: Int,
    val identifierFun: () ⇒ String,
    var clazz: Option[ClassFile])
        extends SourceElementNode {

    def cloneStructure: TypeNode = {
        val clone = new TypeNode(this.uniqueID, this.identifierFun, this.clazz)
        clone.clusterable = this.clusterable
        clone.metaInfo ++= this.metaInfo
        clone
    }

}

case class FieldNode(
    val uniqueID: Int,
    val identifierFun: () ⇒ String,
    var field: Option[Field])
        extends SourceElementNode {

    def cloneStructure: FieldNode = {
        val clone = new FieldNode(this.uniqueID, this.identifierFun, this.field)
        clone.clusterable = this.clusterable
        clone.metaInfo ++= this.metaInfo
        clone
    }

}

case class MethodNode(
    val uniqueID: Int,
    val identifierFun: () ⇒ String,
    var method: Option[Method])
        extends SourceElementNode {

    def cloneStructure: MethodNode = {
        val clone = new MethodNode(this.uniqueID, this.identifierFun, this.method)
        clone.clusterable = this.clusterable
        clone.metaInfo ++= this.metaInfo
        clone
    }
}
