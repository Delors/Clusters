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

trait NodeCloner {
    def createCopy(cluster: Cluster): Cluster =
        new Cluster(cluster.uniqueID, cluster.identifier, cluster.isRootCluster)

    def createCopy(typeNode: TypeNode): TypeNode =
        if (typeNode.clazz.isDefined)
            TypeNode(typeNode.uniqueID, typeNode.identifierFun, typeNode.clazz.get)
        else
            TypeNode(typeNode.uniqueID, typeNode.identifierFun)

    def createCopy(fieldNode: FieldNode): FieldNode =
        if (fieldNode.field.isDefined)
            FieldNode(fieldNode.uniqueID, fieldNode.identifierFun, fieldNode.field.get)
        else
            FieldNode(fieldNode.uniqueID, fieldNode.identifierFun)

    def createCopy(methodNode: MethodNode): MethodNode =
        if (methodNode.method.isDefined)
            MethodNode(methodNode.uniqueID, methodNode.identifierFun, methodNode.method.get)
        else
            MethodNode(methodNode.uniqueID, methodNode.identifierFun)

    def createCopy(node: Node): Node =
        node match {
            case c: Cluster    ⇒ createCopy(c)
            case t: TypeNode   ⇒ createCopy(t)
            case f: FieldNode  ⇒ createCopy(f)
            case m: MethodNode ⇒ createCopy(m)
        }

    def createDeepCopy(
        cluster: Cluster): Cluster = {
        val copy = createCopy(cluster)
        // add copies of cluster elements to the cluster's copy
        cluster.getNodes map {
            createDeepCopy(_)
        } map { node ⇒
            copy.addNode(node)
        }
        copyEdges(cluster, copy)
        copy
    }

    def createDeepCopy(node: Node): Node = {
        node match {
            case c: Cluster ⇒
                createDeepCopy(c)
            case sen: SourceElementNode ⇒
                val copy = createCopy(node)
                copyEdges(node, copy)
                copy
        }
    }

    private def copyEdges(
        node: Node,
        copiedNode: Node) {
        for (edge ← node.getOwnEdges) {
            copiedNode.addEdge(edge.sourceID, edge.targetID, edge.dType, edge.count)
        }
        for (transposedEdge ← node.getTransposedEdges) {
            copiedNode.addEdge(transposedEdge.targetID, transposedEdge.sourceID, transposedEdge.dType, transposedEdge.count)
        }
    }

}