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

import scala.collection.mutable.Queue
import structure.Cluster
import structure.util.ClusterManager
import de.tud.cs.st.bat.resolved.dependency.DependencyExtractor
import de.tud.cs.st.util.perf.PerformanceEvaluation

/**
 * @author Thomas Schlosser
 *
 */
trait ClusteringPipeline extends PerformanceEvaluation {

    // Configurations

    private val clusterings: Queue[Clustering] = new Queue()

    private val clusterManager = ClusterManager()

    private var extractDependencies: (DependencyExtractor) ⇒ Unit = null

    private var resultWriter: ClusteringResultWriter = null

    def addClustering(clustering: Clustering) {
        clusterings += clustering
    }

    def runPipeline(debug: Boolean = false): Cluster = {
        if (debug) {
            debugPipeline()
        }
        else {
            runPipeline()
        }
    }

    private def runPipeline(): Cluster = {
        if (extractDependencies == null) {
            sys.error("A dependency extraction funktion has to be configured to run the pipeline!")
        }

        extractDependencies(clusterManager)

        val result = cluster(clusterManager)

        // export result by calling write method of ClusteringResultWriter trait
        if (resultWriter != null) {
            resultWriter.write(result)
            resultWriter.close() // TODO: check how to handle this on a nice way
        }

        result
    }

    private def debugPipeline(): Cluster = {
        if (extractDependencies == null) {
            sys.error("A dependency extraction funktion has to be configured to run the pipeline!")
        }

        time(duration ⇒ println("Time to read classfiles and extract dependencies: "+nanoSecondsToMilliseconds(duration)+"ms")) {
            extractDependencies(clusterManager)
        }

        println("Number of nodes in root cluster: "+clusterManager.getRootCluster.getNodes.size)

        val result = time(duration ⇒ println("time to cluster input: "+nanoSecondsToMilliseconds(duration)+"ms")) {
            cluster(clusterManager)
        }

        if (resultWriter != null) {
            resultWriter.write(result)
            resultWriter.close() // TODO: check how to handle this on a nice way
        }

        result
    }

    private def cluster(clusterManager: ClusterManager): Cluster = {
        var result = clusterManager.getRootCluster
        clusterings foreach { stage ⇒
            stage.clusterManager = clusterManager
            result = stage.cluster(result)
        }
        result
    }

}

object ClusteringPipeline {

    def apply(
        clusteringArray: Array[Clustering],
        extractDependenciesFunktion: (DependencyExtractor) ⇒ Unit,
        clusteringResultWriter: ClusteringResultWriter = null): ClusteringPipeline =
        new ClusteringPipeline {
            if (clusteringArray != null) {
                clusteringArray foreach { addClustering(_) }
            }
            extractDependencies = extractDependenciesFunktion
            resultWriter = clusteringResultWriter
        }

}