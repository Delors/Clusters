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

import scala.collection.mutable.Map
import de.tud.cs.st.bat.resolved.dependency.SourceElementIDsMap

/**
 * @author thomas
 *
 */
trait NodeStore extends SourceElementIDsMap with ClusterIDsMap {

    protected val INITIAL_ARRAY_SIZE = 100000

    protected val typeNodes: Map[Int, TypeNode] = Map()
    protected val fieldNodes: Map[Int, FieldNode] = Map()
    protected val methodNodes: Map[Int, MethodNode] = Map()
    protected val clusterNodes: Map[Int, Cluster] = Map()

    def storeNode(node: TypeNode) {
        store(node, typeNodes, LOWEST_TYPE_ID)
    }

    def storeNode(node: FieldNode) {
        store(node, fieldNodes, LOWEST_FIELD_ID)
    }

    def storeNode(node: MethodNode) {
        store(node, methodNodes, LOWEST_METHOD_ID)
    }

    def storeNode(node: Cluster) {
        store(node, clusterNodes, LOWEST_CLUSTER_ID)
    }

    def storeNode(node: Node) {
        node match {
            case tn: TypeNode   ⇒ storeNode(tn)
            case fn: FieldNode  ⇒ storeNode(fn)
            case mn: MethodNode ⇒ storeNode(mn)
            case c: Cluster     ⇒ storeNode(c)
        }
    }

    def getNode(id: Int): Node = {
        if (id >= LOWEST_CLUSTER_ID) {
            return get(id, clusterNodes, LOWEST_CLUSTER_ID)
        }
        else if (id >= LOWEST_METHOD_ID) {
            return get(id, methodNodes, LOWEST_METHOD_ID)
        }
        else if (id >= LOWEST_FIELD_ID) {
            return get(id, fieldNodes, LOWEST_FIELD_ID)
        }
        else if (id >= LOWEST_TYPE_ID) {
            return get(id, typeNodes, LOWEST_TYPE_ID)
        }
        sys.error("No mapping found for ID["+id+"]")
    }

    def getCluster(id: Int): Cluster = {
        if (id >= LOWEST_CLUSTER_ID) {
            return get(id, clusterNodes, LOWEST_CLUSTER_ID)
        }
        sys.error("No cluster mapping found for ID["+id+"]")
    }

    def getMethodNode(id: Int): MethodNode = {
        if (id < LOWEST_CLUSTER_ID && id >= LOWEST_METHOD_ID) {
            return get(id, methodNodes, LOWEST_METHOD_ID)
        }
        sys.error("No methode node mapping found for ID["+id+"]")
    }

    def getFieldNode(id: Int): FieldNode = {
        if (id < LOWEST_METHOD_ID && id >= LOWEST_FIELD_ID) {
            return get(id, fieldNodes, LOWEST_FIELD_ID)
        }
        sys.error("No field node mapping found for ID["+id+"]")
    }

    def getTypeNode(id: Int): TypeNode = {
        if (id < LOWEST_FIELD_ID && id >= LOWEST_TYPE_ID) {
            return get(id, typeNodes, LOWEST_TYPE_ID)
        }
        sys.error("No type node mapping found for ID["+id+"]")
    }

    private def get[N <: Node](id: Int, nodes: Map[Int, N], lowestID: Int): N = {
        val index = id - lowestID
        nodes.getOrElse(index, null.asInstanceOf[N])
    }

    private def store[N <: Node](node: N, nodes: Map[Int, N], lowestID: Int) {
        val index = node.uniqueID - lowestID
        nodes(index) = node
    }
}