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
package util

import de.tud.cs.st.bat.resolved.Type
import de.tud.cs.st.bat.resolved.Field
import de.tud.cs.st.bat.resolved.Method
import de.tud.cs.st.bat.resolved.ClassFile
import de.tud.cs.st.bat.resolved.ObjectType
import de.tud.cs.st.bat.resolved.ReferenceType
import de.tud.cs.st.bat.resolved.MethodDescriptor

/**
 * @author Thomas Schlosser
 *
 */
trait NodeFactory extends PrettyPrint with NodeStore {

    def createTypeNode(id: Int, classFile: ClassFile) {
        createNode(
            id,
            (oldNode: TypeNode) ⇒ oldNode.clazz = Some(classFile),
            (id) ⇒ new TypeNode(id, () ⇒ prettyPrint(classFile.thisClass), Some(classFile)))
    }

    def createTypeNode(id: Int, t: Type): TypeNode =
        createNode(
            id,
            (c: TypeNode) ⇒ (),
            (id) ⇒ new TypeNode(id, () ⇒ prettyPrint(t), None))

    def createFieldNode(id: Int, classFile: ClassFile, field: Field) {
        createNode(
            id,
            (oldNode: FieldNode) ⇒ oldNode.field = Some(field),
            (id) ⇒ new FieldNode(id, () ⇒ prettyPrint(classFile.thisClass, field.name), Some(field)))
    }

    def createFieldNode(id: Int, definingObjectType: ObjectType, fieldName: String): FieldNode =
        createNode(
            id,
            (c: FieldNode) ⇒ (),
            (id) ⇒ new FieldNode(id, () ⇒ prettyPrint(definingObjectType, fieldName), None))

    def createMethodNode(id: Int, classFile: ClassFile, method: Method) {
        createNode(
            id,
            (oldNode: MethodNode) ⇒ oldNode.method = Some(method),
            (id) ⇒ new MethodNode(id, () ⇒ prettyPrint(classFile.thisClass, method.name, method.descriptor), Some(method)))
    }

    def createMethodNode(id: Int, definingReferenceType: ReferenceType, methodName: String, methodDescriptor: MethodDescriptor): MethodNode =
        createNode(
            id,
            (c: MethodNode) ⇒ (),
            (id) ⇒ new MethodNode(id, () ⇒ prettyPrint(definingReferenceType, methodName, methodDescriptor), None))

    private def createNode[N <: Node](
        id: Int,
        nodeExistsAction: (N) ⇒ _ = (n: N) ⇒ (),
        newNode: (Int) ⇒ N): N = {
        val oldNode = getNode(id).asInstanceOf[N]
        if (oldNode != null) {
            nodeExistsAction(oldNode)
            oldNode
        }
        else {
            val node = newNode(id)
            storeNode(node)
            node
        }
    }
}
