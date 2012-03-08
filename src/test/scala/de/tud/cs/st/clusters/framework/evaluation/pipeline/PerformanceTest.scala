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
import util.SourceFile
import framework.structure.Cluster
import framework.structure.util.ClusterManager
import de.tud.cs.st.util.perf._

/**
 * @author Thomas Schlosser
 *
 */
class PerformanceTest extends AbstractEvaluationTest {

    protected def evaluate(
        testName: String,
        sourceFiles: SourceFile,
        referenceClusteringFilePath: String,
        allStageCombos: Array[(String, Array[ClusteringStage])]) {
        println(testName+" - START")

        var i = 1
        allStageCombos foreach { combo ⇒
            val (comboName, stageCombo) = combo
            println("run combination #"+i+" ("+comboName+")")
            var clusteringPipeline =
                new ClusteringPipeline(
                    stageCombo,
                    None) with PerformanceEvaluatedPipeline

            1 to 10 foreach { x ⇒
                clusteringPipeline.runPipeline(sourceFiles)
            }
            clusteringPipeline.printStatistics()
            i += 1
        }

        println(testName+" - END\n")
    }

    trait PerformanceEvaluatedPipeline
            extends ClusteringPipeline {

        //        protected abstract override def runDependencyExtraction(sourceFiles: SourceFile*): ClusterManager = {
        //            time(duration ⇒ println("Time to read classfiles and extract dependencies: "+nanoSecondsToMilliseconds(duration)+"ms")) {
        //                super.runDependencyExtraction(sourceFiles: _*)
        //            }
        //        }

        var durations: List[Long] = Nil

        protected abstract override def runClustering(clusterManager: ClusterManager): Cluster = {
            time(duration ⇒ {
                println("cluster time: "+nanoSecondsToMilliseconds(duration)+"ms")
                durations = duration :: durations
            }) {
                super.runClustering(clusterManager)
            }
        }

        def printStatistics() {
            val min = (Long.MaxValue /: durations) { math.min(_, _) }
            val max = (0l /: durations) { math.max(_, _) }
            val avg = (0l /: durations) { _ + _ } / durations.length
            val (lower, upper) = durations.sortWith(_ < _).splitAt(durations.size / 2)
            val med = if (durations.size % 2 == 0) (lower.last + upper.head) / 2 else upper.head
            println("cluster time (min): "+nanoSecondsToMilliseconds(min)+"ms")
            println("cluster time (max): "+nanoSecondsToMilliseconds(max)+"ms")
            println("cluster time (avg): "+nanoSecondsToMilliseconds(avg)+"ms")
            println("cluster time (med): "+nanoSecondsToMilliseconds(med)+"ms")
        }

    }
}

