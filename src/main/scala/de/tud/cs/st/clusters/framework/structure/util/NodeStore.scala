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
import de.tud.cs.st.bat.resolved.CategorizedSourceElementIDs

/**
 * @author Thomas Schlosser
 *
 */
trait NodeStore extends CategorizedSourceElementIDs with CategorizedClusterIDs {

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

    /**
     * Gets the node with the given ID from the store.
     * A precondition of this method is, that the given ID has to be a valid node ID.
     * Valid node IDs have to be greater than [[de.tud.cs.st.bat.resolved.CategorizedSourceElementIDs.LOWEST_TYPE_ID]].
     * Hence, it is not allowed to pass invalid IDs.
     *
     * @param id The ID of the node that should be retrieved from the store.
     * @return The node with the given ID if the precondition was met and a node
     *         with the given ID was stored in the store.
     *         Otherwise, the current implementation returns NULL.
     */
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
        return get(id, typeNodes, LOWEST_TYPE_ID)
    }

    /**
     * Gets the cluster with the given ID from the store.
     * A precondition of this method is, that the given ID has to be a valid cluster ID.
     * Valid cluster IDs have to be greater or equal to [[de.tud.cs.st.clusters.framework.structure.util.CategorizedClusterIDs.LOWEST_CLUSTER_ID]].
     * Hence, it is not allowed to pass invalid IDs.
     *
     * @param id The ID of the cluster that should be retrieved from the store.
     * @return The cluster with the given ID if the precondition was met and a cluster
     *         with the given ID was stored in the store.
     *         Otherwise, the current implementation returns NULL.
     */
    def getCluster(id: Int): Cluster =
        get(id, clusterNodes, LOWEST_CLUSTER_ID)

    /**
     * Gets the method node with the given ID from the store.
     * A precondition of this method is, that the given ID has to be a valid method node ID.
     * Valid method node IDs have to be greater or equal to [[de.tud.cs.st.bat.resolved.CategorizedSourceElementIDs.LOWEST_METHOD_ID]]
     * and less than [[de.tud.cs.st.clusters.framework.structure.util.CategorizedClusterIDs.LOWEST_CLUSTER_ID]]
     * Hence, it is not allowed to pass invalid IDs.
     *
     * @param id The ID of the method node that should be retrieved from the store.
     * @return The method node with the given ID if the precondition was met and
     *         a method node with the given ID was stored in the store.
     *         Otherwise, the current implementation returns NULL.
     */
    def getMethodNode(id: Int): MethodNode =
        get(id, methodNodes, LOWEST_METHOD_ID)

    /**
     * Gets the field node with the given ID from the store.
     * A precondition of this method is, that the given ID has to be a valid field node ID.
     * Valid field node IDs have to be greater or equal to [[de.tud.cs.st.bat.resolved.CategorizedSourceElementIDs.LOWEST_FIELD_ID]]
     * and less than [[de.tud.cs.st.bat.resolved.CategorizedSourceElementIDs.LOWEST_METHOD_ID]]
     * Hence, it is not allowed to pass invalid IDs.
     *
     * @param id The ID of the field node that should be retrieved from the store.
     * @return The field node with the given ID if the precondition was met and
     *         a field node with the given ID was stored in the store.
     *         Otherwise, the current implementation returns NULL.
     */
    def getFieldNode(id: Int): FieldNode =
        get(id, fieldNodes, LOWEST_FIELD_ID)

    /**
     * Gets the type node with the given ID from the store.
     * A precondition of this method is, that the given ID has to be a valid type node ID.
     * Valid type node IDs have to be greater or equal to [[de.tud.cs.st.bat.resolved.CategorizedSourceElementIDs.LOWEST_TYPE_ID]]
     * and less than [[de.tud.cs.st.bat.resolved.CategorizedSourceElementIDs.LOWEST_FIELD_ID]]
     * Hence, it is not allowed to pass invalid IDs.
     *
     * @param id The ID of the type node that should be retrieved from the store.
     * @return The type node with the given ID if the precondition was met and
     *         a type node with the given ID was stored in the store.
     *         Otherwise, the current implementation returns NULL.
     */
    def getTypeNode(id: Int): TypeNode =
        get(id, typeNodes, LOWEST_TYPE_ID)

    private def get[N <: Node](id: Int, nodes: Map[Int, N], lowestID: Int): N = {
        val index = id - lowestID
        nodes.getOrElse(index, null.asInstanceOf[N])
    }

    private def store[N <: Node](node: N, nodes: Map[Int, N], lowestID: Int) {
        val index = node.uniqueID - lowestID
        nodes(index) = node
    }
}