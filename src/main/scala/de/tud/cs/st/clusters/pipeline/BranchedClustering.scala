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
package pipeline

import framework.pipeline.Clustering
import framework.pipeline.IntermediateClustering
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
class BranchedClustering(
    val builder: ClusterBuilder,
    val successorClustering: Option[Clustering],
    val clusterings: Clustering*)
        extends Clustering {

    override def process(clusters: Array[Cluster]): Array[Cluster] = {
        val results = clusters map { NodeCloner.createCopy(_) }
        clusters.zipWithIndex foreach {
            case (inputCluster, i) ⇒
                val resultCluster = results(i)
                clusterings.zipWithIndex foreach {
                    case (f, j) ⇒
                        // no need to retrieve a unique number for this temporary cluster
                        val splitResult = builder.createCluster("split_result_"+j) //new Cluster(-1, "split_result_"+j)
                        f.process(Array(inputCluster)) foreach {
                            _.getNodes foreach {
                                splitResult.addNode(_)
                            }
                        }
                        resultCluster.addNode(splitResult)
                }
        }
        mergeClusters(results)
    }

    /**
     * @param clusters Contains clusters with one cluster per clustering threads in it which have to be merged.
     *  These inner clusters contain the result of the corresponding clustering thread.
     *  Hence, the parameter has the following structure:
     *  Array(
     *    Cluster(identifier="<identifier of first cluster>",
     *            nodes=
     *              Cluster(identifier="split_result_0",
     *                      nodes=<results of [[SplitCluster]]'s first clustering applied to the first cluster>),
     *              Cluster(identifier="split_result_1",
     *                      nodes=<results of [[SplitCluster]]'s second clustering applied to the first cluster>),
     *              ...
     *    ),
     *    ...
     *  )
     */
    def mergeClusters(clusters: Array[Cluster]): Array[Cluster] = {
        //TODO: impl. and test this split and merge mechanism...
        val result = clusters map { NodeCloner.createCopy(_) }
        clusters.zipWithIndex foreach {
            case (inputCluster, i) ⇒
                val resultCluster = result(i)
                // TODO do something with all the splitCluster results 
                // and add the resulting clusters to 'resultCluster' 
                inputCluster.getNodes foreach {
                    case splitCluster: Cluster ⇒
                    //                    process(splitCluster)
                }
        }
        if (successorClustering.isDefined)
            return successorClustering.get.process(result)
        else
            return clusters //result
    }
}

object BranchedClustering {

    def apply(
        clusterBuilder: ClusterBuilder,
        successorClustering: Clustering = null)(
            clusterings: Clustering*): BranchedClustering =
        new BranchedClustering(
            clusterBuilder,
            if (successorClustering == null) None else Some(successorClustering),
            clusterings: _*)

}
