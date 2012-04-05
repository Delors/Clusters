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
package evaluation
package pipeline

import framework.pipeline.ClusteringPipeline
import framework.pipeline.ClusteringStage
import util.ReferenceClusterCreator
import evaluation.MoJoWrapper
import util.SourceFile
import framework.structure.Cluster
import framework.structure.SourceElementNode
import de.tud.cs.st.util.perf._
import org.junit.rules.TestName
import de.tud.cs.st.clusters.framework.evaluation.ClusterStatistics

/**
 * @author Thomas Schlosser
 *
 */
class QualityTest extends AbstractEvaluationTest {

    final val mojoFM = "MoJoFM"
    final val mojoHM = "MoJoHM"

    val mojoVariant = mojoHM

    protected final def evaluate(
        projectName: String,
        sourceFiles: SourceFile,
        referenceClusteringFilePath: String,
        testRuns: Int,
        measuredRuns: Int,
        allStageCombos: Array[(String, Array[ClusteringStage])]) {
        val testName = "evaluate ["+projectName+"]"

        if (referenceClusteringFilePath == null)
            sys.error("No reference clustering file is given!")

        val referenceCluster = ReferenceClusterCreator.readReferenceCluster(
            sourceFiles,
            new java.io.File(referenceClusteringFilePath))

        beforeEvaluation(testName, projectName, referenceCluster)

        var i = 1
        allStageCombos foreach { combo ⇒
            val (comboName, stageCombo) = combo

            beforeStageEvaluation(i, testName, projectName, comboName)

            var clusteringPipeline: ClusteringPipeline =
                new ClusteringPipeline(stageCombo, None)

            val extractedCluster = clusteringPipeline.runPipeline(sourceFiles)

            var mjw = new MoJoWrapper(extractedCluster, referenceCluster)
            val maxLevelReferenceCluster = mjw.maxDepthAuthorativeCluster
            val maxLevelExtractedCluster = mjw.maxDepthCalculatedCluster
            val levelLimit = scala.math.max(maxLevelReferenceCluster, maxLevelExtractedCluster)

            afterStageEvaluation(i, testName, projectName, comboName, mjw, extractedCluster, maxLevelReferenceCluster, maxLevelExtractedCluster, levelLimit)

            i += 1
        }

        afterEvaluation(testName, projectName)
    }

    protected def beforeEvaluation(testName: String, projectName: String, referenceCluster: Cluster) {
        println(testName+" - START")
        ClusterStatistics.printStatistics(referenceCluster)
    }

    protected def beforeStageEvaluation(comboNumber: Int, testName: String, projectName: String, comboName: String) {
        println("run combination #"+comboNumber+" ("+comboName+")")
    }

    protected def afterStageEvaluation(comboNumber: Int, testName: String, projectName: String, comboName: String, mjw: MoJoWrapper, extractedCluster: Cluster, maxLevelReferenceCluster: Int, maxLevelExtractedCluster: Int, levelLimit: Int) {
        1 to levelLimit foreach { level ⇒
            mjw.levelLimit = Some(level)
            if (mojoVariant == mojoHM) {
                println(mojoVariant+"(level="+level+"): "+mjw.singleDirectionMoJoHM)
            }
            else if (mojoVariant == mojoFM) {
                println(mojoVariant+"(level="+level+"): "+mjw.singleDirectionMoJoFM)
            }
        }

        println("Levels (ReferenceCluster):"+maxLevelReferenceCluster)
        println("Levels (ExctractedCluster):"+maxLevelExtractedCluster)
        ClusterStatistics.printStatistics(extractedCluster, true)
    }

    protected def afterEvaluation(testName: String, projectName: String) {
        println(testName+" - END\n")
    }
}

