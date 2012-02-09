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
 * Creates clones of nodes that contain clones of all child nodes.
 * Edges are not considered during the clone process. All cloned
 * nodes do not contain edges.
 *
 * @author Thomas Schlosser
 *
 */
// TODO Currently, I'm not sure why you didn't simply implement the cloning related functionality directly on the relevant types themselves?
trait NodeCloner {

    def cloneNodeStructure(
        cluster: Cluster): Cluster = {
        val clone = cloneNode(cluster)
        // add clones of cluster elements to the cluster's clone
        cluster.nodes map {
            cloneNodeStructure(_)
        } map { node ⇒
            clone.addNode(node)
        }
        clone
    }

    def cloneNodeStructure(node: Node): Node = {
        node match {
            case c: Cluster ⇒
                cloneNodeStructure(c)
            case sen: SourceElementNode ⇒
                cloneNode(node)
        }
    }

    private def cloneNode(cluster: Cluster): Cluster =
        new Cluster(cluster.uniqueID, cluster.identifier)

    private def cloneNode(typeNode: TypeNode): TypeNode =
        if (typeNode.clazz.isDefined)
            new TypeNode(typeNode.uniqueID, typeNode.identifierFun, typeNode.clazz)
        else
            new TypeNode(typeNode.uniqueID, typeNode.identifierFun, None)

    private def cloneNode(fieldNode: FieldNode): FieldNode =
        if (fieldNode.field.isDefined)
            new FieldNode(fieldNode.uniqueID, fieldNode.identifierFun, fieldNode.field)
        else
            new FieldNode(fieldNode.uniqueID, fieldNode.identifierFun, None)

    private def cloneNode(methodNode: MethodNode): MethodNode =
        if (methodNode.method.isDefined)
            new MethodNode(methodNode.uniqueID, methodNode.identifierFun, methodNode.method)
        else
            new MethodNode(methodNode.uniqueID, methodNode.identifierFun, None)

    private def cloneNode(node: Node): Node =
        node match {
            case c: Cluster    ⇒ cloneNode(c)
            case t: TypeNode   ⇒ cloneNode(t)
            case f: FieldNode  ⇒ cloneNode(f)
            case m: MethodNode ⇒ cloneNode(m)
        }

}