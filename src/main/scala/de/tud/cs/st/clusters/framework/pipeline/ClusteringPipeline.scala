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

import util.DependencyExtractionUtils
import util.SourceFile
import de.tud.cs.st.bat.resolved.dependency.DependencyExtractor
import structure.util.ClusterManager
import structure.util.DefaultDependencyExtractor
import structure.Cluster
import result.ClusteringResultWriter

/**
 * @author Thomas Schlosser
 *
 */
class ClusteringPipeline(
        protected val clusteringStages: Traversable[ClusteringStage],
        protected val createConcreteClusteringResultWriter: Option[() ⇒ ClusteringResultWriter] = None) {

    final def runPipeline(sourceFiles: SourceFile*): Cluster = {
        val clusterManager = runDependencyExtraction(sourceFiles: _*)

        val result = runClustering(clusterManager)

        runExport(result)

        result
    }

    protected def runDependencyExtraction(sourceFiles: SourceFile*): ClusterManager = {
        val dependencyExtractor = new DefaultDependencyExtractor()
        val extractDependencies = DependencyExtractionUtils.extractDependencies(dependencyExtractor)_
        sourceFiles foreach { sourceFile ⇒
            extractDependencies(sourceFile)
        }
        dependencyExtractor.clusterManager
    }

    protected def runClustering(clusterManager: ClusterManager): Cluster = {
        var projectCluster = clusterManager.getProjectCluster
        clusteringStages foreach { stage ⇒
            stage.clusterManager = clusterManager
            stage.performClustering(projectCluster)
            stage.clusterManager = null
        }
        projectCluster
    }

    protected def runExport(result: Cluster) {
        // export result by calling write method of concrete ClusteringResultWriter trait
        if (createConcreteClusteringResultWriter.isDefined) {
            val resultWriter = createConcreteClusteringResultWriter.get()
            if (resultWriter != null) {
                try {
                    resultWriter.write(result)
                }
                finally {
                    resultWriter.close()
                }
            }
        }
    }
}
