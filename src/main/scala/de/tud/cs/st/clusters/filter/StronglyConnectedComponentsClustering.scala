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
import structure.Cluster
import java.io.File
import de.tud.cs.st.bat.resolved.DependencyType._
import structure.Node
import de.tud.cs.st.clusters.filter.graphscan.GraphScanResultBean
import de.tud.cs.st.clusters.filter.graphscan.GraphScanningAlgorithms
import de.tud.cs.st.clusters.structure.TransposedCluster
import scala.collection.mutable.Map

/**
 * @author Thomas Schlosser
 *
 */
trait StronglyConnectedComponentsClustering extends ClusterFilter {
  abstract override def filter(clusters: Array[Cluster], projectRootDir: File): Array[Cluster] = {
    for (cluster <- clusters) {
      createStronglyConnectedComponents(cluster)
    }
    super.filter(clusters, projectRootDir)
    clusters
  }

  private def createStronglyConnectedComponents(cluster: Cluster) {
    // calculate finishing times of all nodes using depth first search
    var result = GraphScanningAlgorithms.graphScanComplete(
      cluster, 0, true, null)

    // calculate depth first search on the transposed cluster considering
    // the finishing times of the first run of the depth first search algorithm
    result = GraphScanningAlgorithms.graphScanComplete(cluster,
      null, true, result.order)(true)

    // create resulting clusters
    var resultMap = Map[Int, Cluster]()
    for (node <- cluster.nodes) {
      val sccID = result.color(node.uniqueID) - 2
      if (sccID >= 0) {
        resultMap.get(sccID) match {
          case Some(c) =>
            c.nodes :+= node
          case None =>
            val c = new Cluster("SCC_" + sccID)
            c.nodes :+= node
            resultMap(sccID) = c
        }
      }
    }

    cluster.nodes = resultMap.values.toList
  }
}