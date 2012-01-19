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
package pipeline

import structure.Cluster
import structure.util.ClusterManager

/**
 * Trait that represents a stage in the clustering pipeline. It defines an abstract method
 * 'process' which is called from the [[de.tud.cs.st.clusters.framework.pipeline.ClusteringPipeline]]
 * to start the clustering process or a part of the whole clustering process, respectively.
 * Implementations of this trait/method define the way how the root cluster or parts of it
 * are passed to the cluster stage.
 *
 * @author Thomas Schlosser
 */
trait ClusteringStage[C <: ClusteringStageConfiguration] {

    protected val configuration: C

    private[this] var cm: ClusterManager = null
    def clusterManager: ClusterManager = cm
    def clusterManager_=(cm: ClusterManager) { this.cm = cm }

    //TODO: make this method public; comment intension
    protected def process(cluster: Cluster): Cluster

    private[pipeline] def cluster(cluster: Cluster): Cluster =
        process(cluster)

}

trait ClusteringStageConfiguration {

}