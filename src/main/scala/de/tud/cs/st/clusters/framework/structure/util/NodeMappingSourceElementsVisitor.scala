package de.tud.cs.st.clusters
package framework
package structure
package util

import scala.collection.mutable.ArrayBuffer
import de.tud.cs.st.bat.resolved.SourceElementsVisitor
import de.tud.cs.st.bat.resolved.ClassFile
import de.tud.cs.st.bat.resolved.Method
import de.tud.cs.st.bat.resolved.Field
import de.tud.cs.st.bat.resolved.dependency.SourceElementIDs

/**
 * Implementation of the SourceElementsVisitor trait where all source elements
 * are added to the corresponding node in the lookup buffer. If there is no node
 * for the given source element, a new node that directly contains the source element
 * is added.
 *
 * @author Thomas Schlosser
 */
trait NodeMappingSourceElementsVisitor extends SourceElementsVisitor[Unit]
        with SourceElementIDs
        with PrettyPrint {

    override def visit(classFile: ClassFile) {
        handleIDLookup(
            () ⇒ super.sourceElementID(classFile),
            LOWEST_TYPE_ID,
            typeNodes,
            (oldNode: TypeNode) ⇒ oldNode.clazz = Some(classFile),
            (id) ⇒ TypeNode(id, () ⇒ prettyPrint(classFile.thisClass), classFile))
    }

    override def visit(classFile: ClassFile, method: Method) {
        handleIDLookup(
            () ⇒ super.sourceElementID(classFile.thisClass, method),
            LOWEST_METHOD_ID,
            methodNodes,
            (oldNode: MethodNode) ⇒ oldNode.method = Some(method),
            (id) ⇒ MethodNode(id, () ⇒ prettyPrint(classFile.thisClass, method.name, method.descriptor), method))
    }

    override def visit(classFile: ClassFile, field: Field) {
        handleIDLookup(
            () ⇒ super.sourceElementID(classFile.thisClass, field),
            LOWEST_FIELD_ID,
            fieldNodes,
            (oldNode: FieldNode) ⇒ oldNode.field = Some(field),
            (id) ⇒ FieldNode(id, () ⇒ prettyPrint(classFile.thisClass, field.name), field))
    }

    protected def LOWEST_TYPE_ID: Int
    protected def LOWEST_METHOD_ID: Int
    protected def LOWEST_FIELD_ID: Int

    protected def typeNodes: ArrayBuffer[TypeNode]
    protected def methodNodes: ArrayBuffer[MethodNode]
    protected def fieldNodes: ArrayBuffer[FieldNode]

    protected def handleIDLookup[N <: Node](
        lookupId: () ⇒ Int,
        lowestId: Int,
        nodes: ArrayBuffer[N],
        nodeExistsAction: (N) ⇒ Unit,
        newNode: (Int) ⇒ N,
        addNodeToRoot: Boolean = true): Int
}