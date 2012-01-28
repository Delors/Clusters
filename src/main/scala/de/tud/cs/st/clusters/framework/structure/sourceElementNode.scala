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

    val uniqueID: Int

    lazy val identifier = identifierFun()
    def identifierFun: () ⇒ String

    override var clusterable = false

}

case class TypeNode private (
    val uniqueID: Int,
    val identifierFun: () ⇒ String,
    var clazz: Option[ClassFile])
        extends SourceElementNode {

    def this(id: Int, identifierFun: () ⇒ String, clazz: ClassFile) {
        this(id, identifierFun, Some(clazz))
    }

    def this(id: Int, identifierFun: () ⇒ String) {
        this(id, identifierFun, None)
    }

}

case class FieldNode private (
    val uniqueID: Int,
    val identifierFun: () ⇒ String,
    var field: Option[Field])
        extends SourceElementNode {

    def this(id: Int, identifierFun: () ⇒ String, field: Field) {
        this(id, identifierFun, Some(field))
    }

    def this(id: Int, identifierFun: () ⇒ String) {
        this(id, identifierFun, None)
    }

}

case class MethodNode private (
    val uniqueID: Int,
    val identifierFun: () ⇒ String,
    var method: Option[Method])
        extends SourceElementNode {

    def this(id: Int, identifierFun: () ⇒ String, method: Method) {
        this(id, identifierFun, Some(method))
    }

    def this(id: Int, identifierFun: () ⇒ String) {
        this(id, identifierFun, None)
    }

}
