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
import framework.structure.NodeCloner
import framework.structure.util.ClusterBuilder

/**
 * @author Thomas Schlosser
 *
 */
class SplitClustering(
    val builder: ClusterBuilder,
    val mergeFilter: MergeClustering,
    val filters: ClusterFilter*)
        extends ClusterFilter {

    override def process(clusters: Array[Cluster]): Array[Cluster] = {
        val splitResults = clusters map { NodeCloner.createCopy(_) }
        for (cluster ← clusters) {
            filters.zipWithIndex foreach {
                case (f, i) ⇒
                    // no need to retrieve a unique number for this temporary cluster
                    val splitResult = new Cluster(-1, "split_result_"+i)
                    f.process(Array(cluster)) foreach {
                        _.getNodes foreach {
                            splitResult.addNode(_)
                        }
                    }
                    splitResults(i).addNode(splitResult)
            }
        }
        mergeFilter.process(splitResults)
    }
}

object SplitClustering {

    def apply(
        clusterBuilder: ClusterBuilder,
        mergeFilter: MergeClustering,
        filters: ClusterFilter*): SplitClustering =
        new SplitClustering(
            clusterBuilder,
            if (mergeFilter != null) mergeFilter else sys.error("A corresponding MergeClustering has to be configured!"),
            filters: _*)

}
