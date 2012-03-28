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

/**
 * @author Thomas Schlosser
 *
 */
trait ClusterFactory
        extends ClusterIDs
        with NodeStore {

    private var nextGloballyUniqueID = 0
    private def globallyUniqueID: Int = {
        val id = nextGloballyUniqueID
        nextGloballyUniqueID += 1
        id
    }

    def createCluster(clusterName: String, creatorName: String, makeUnique: Boolean = false): Cluster = {
        val uniqueName = if (makeUnique) (clusterName + globallyUniqueID) else clusterName
        val cluster = createClusterInStore(
            clusterID(uniqueName),
            (c: Cluster) ⇒ (),
            (id) ⇒ new Cluster(id, ClusterIdentifier(clusterName, uniqueName)))
        if (cluster.metaInfo.isDefinedAt("creator")) {
            cluster.metaInfo("lastExplicitAccessor") = creatorName
        }
        else {
            cluster.metaInfo("creator") = creatorName
        }
        cluster
    }

    private def createClusterInStore[N <: Node](id: Int,
                                                nodeExistsAction: (N) ⇒ _,
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
