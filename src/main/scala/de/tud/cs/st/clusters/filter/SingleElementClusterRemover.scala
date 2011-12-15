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
package filter

import framework.filter.ClusterFilter
import framework.filter.IntermediateClusterFilter
import framework.structure.Cluster
import framework.structure.TypeNode
import framework.structure.FieldNode
import framework.structure.MethodNode
import framework.structure.util.ClusterBuilder
import de.tud.cs.st.clusters.framework.structure.SourceElementNode

/**
 * @author Thomas Schlosser
 *
 */
class SingleElementClusterRemover(
        val builder: ClusterBuilder,
        val successorFilter: Option[ClusterFilter],
        val clusterNewFilter: Option[ClusterFilter]) extends IntermediateClusterFilter {

    //TODO re-check whether it is OK that in this case no copies of the cluster and the nodes are created  
    protected def process(cluster: Cluster): Cluster = {
        cluster.getNodes foreach {
            case c: Cluster ⇒
                if (c.numberOfNodes == 1) {
                    cluster.addNode(c.getNodes.first)
                    cluster.removeNode(c.uniqueID)
                }
            case _: SourceElementNode ⇒ // Nothing to do, because SourceElementNodes represent basic nodes and no clusters. 
        }
        cluster
    }
}

object SingleElementClusterRemover {

    def apply(
        clusterBuilder: ClusterBuilder,
        successorFilter: ClusterFilter = null,
        clusterNewFilter: ClusterFilter = null): SingleElementClusterRemover =
        new SingleElementClusterRemover(
            clusterBuilder,
            if (successorFilter == null) None else Some(successorFilter),
            if (clusterNewFilter == null) None else Some(clusterNewFilter))

}
