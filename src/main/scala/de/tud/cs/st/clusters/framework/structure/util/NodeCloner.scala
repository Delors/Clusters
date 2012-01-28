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

/**
 * Creates copies of nodes that contain copies of all child nodes.
 * Edges are not considered during the copy process. All copied
 * nodes do not contain edges.
 *
 * @author Thomas Schlosser
 *
 */
trait NodeCloner {

    def cloneNodeStructure(
        cluster: Cluster): Cluster = {
        val copy = createCopy(cluster)
        // add copies of cluster elements to the cluster's copy
        cluster.getNodes map {
            cloneNodeStructure(_)
        } map { node ⇒
            copy.addNode(node)
        }
        copy
    }

    def cloneNodeStructure(node: Node): Node = {
        node match {
            case c: Cluster ⇒
                cloneNodeStructure(c)
            case sen: SourceElementNode ⇒
                val copy = createCopy(node)
                copy
        }
    }

    private def createCopy(cluster: Cluster): Cluster =
        new Cluster(cluster.uniqueID, cluster.identifier, cluster.isRootCluster)

    private def createCopy(typeNode: TypeNode): TypeNode =
        if (typeNode.clazz.isDefined)
            new TypeNode(typeNode.uniqueID, typeNode.identifierFun, typeNode.clazz.get)
        else
            new TypeNode(typeNode.uniqueID, typeNode.identifierFun)

    private def createCopy(fieldNode: FieldNode): FieldNode =
        if (fieldNode.field.isDefined)
            new FieldNode(fieldNode.uniqueID, fieldNode.identifierFun, fieldNode.field.get)
        else
            new FieldNode(fieldNode.uniqueID, fieldNode.identifierFun)

    private def createCopy(methodNode: MethodNode): MethodNode =
        if (methodNode.method.isDefined)
            new MethodNode(methodNode.uniqueID, methodNode.identifierFun, methodNode.method.get)
        else
            new MethodNode(methodNode.uniqueID, methodNode.identifierFun)

    private def createCopy(node: Node): Node =
        node match {
            case c: Cluster    ⇒ createCopy(c)
            case t: TypeNode   ⇒ createCopy(t)
            case f: FieldNode  ⇒ createCopy(f)
            case m: MethodNode ⇒ createCopy(m)
        }

}