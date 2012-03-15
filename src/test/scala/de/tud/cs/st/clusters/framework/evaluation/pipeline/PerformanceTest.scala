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
import framework.structure.Node
import framework.structure.SourceElementNode
import framework.structure.TypeNode
import framework.structure.FieldNode
import framework.structure.MethodNode
import framework.structure.util.ClusterManager
import framework.structure.util.NodeStore
import de.tud.cs.st.util.perf._
import de.tud.cs.st.clusters.framework.structure.util.DefaultClusterManager

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
            print("run combination #"+i+" ("+comboName+")")

            val referenceClusteringPipeline = new ClusteringPipeline(stageCombo, None)
            val refCluster = referenceClusteringPipeline.runPipeline(sourceFiles)

            var clusteringPipeline =
                new ClusteringPipeline(
                    stageCombo,
                    None) with PerformanceEvaluatedPipeline with ClusterCachedPipeline {
                    val numberOfTestRuns = 50
                }

            1 to 100 foreach { x ⇒
                val cl = clusteringPipeline.runPipeline(sourceFiles)
                val mojoHM = new MoJoWrapper(cl, refCluster).singleDirectionMoJoHM
                if (mojoHM != 100.0) {
                    println("ERROR: wrong result in iteration ["+x+"] mojoHM:["+mojoHM+"]")
                }
            }
            clusteringPipeline.printStatistics()
            i += 1
        }

        println(testName+" - END\n")
    }

    trait PerformanceEvaluatedPipeline
            extends ClusteringPipeline {

        val numberOfTestRuns: Int
        var runCounter = 0

        var durations: List[Long] = Nil

        protected abstract override def runClustering(clusterManager: ClusterManager): Cluster = {
            if (runCounter < numberOfTestRuns) {
                print(".")
                runCounter += 1
                if (runCounter == numberOfTestRuns)
                    println()
                super.runClustering(clusterManager)
            }
            else {
                runCounter += 1
                time(duration ⇒ {
                    //println("cluster time: "+nanoSecondsToMilliseconds(duration)+"ms")
                    durations = duration :: durations
                }) {
                    super.runClustering(clusterManager)
                }
            }
        }

        def printStatistics() {
            val min = (Long.MaxValue /: durations) { math.min(_, _) }
            val max = (0l /: durations) { math.max(_, _) }
            val avg = (0l /: durations) { _ + _ } / durations.length
            val (lower, upper) = durations.sortWith(_ < _).splitAt(durations.size / 2)
            val med = if (durations.size % 2 == 0) (lower.last + upper.head) / 2 else upper.head

            println("measured runs(test runs): "+durations.size+"("+numberOfTestRuns+")")
            println("cluster time (min): "+nanoSecondsToMilliseconds(min)+"ms")
            println("cluster time (max): "+nanoSecondsToMilliseconds(max)+"ms")
            println("cluster time (avg): "+nanoSecondsToMilliseconds(avg)+"ms")
            println("cluster time (med): "+nanoSecondsToMilliseconds(med)+"ms")
        }
    }

    trait ClusterCachedPipeline
            extends ClusteringPipeline {

        var cachedClusterManager: ClusterManager = null
        var cachedSourceElements: Set[Node] = null

        protected abstract override def runDependencyExtraction(sourceFiles: SourceFile*): ClusterManager = {
            if (cachedClusterManager == null) {
                cachedClusterManager = super.runDependencyExtraction(sourceFiles: _*)
                cachedSourceElements = cachedClusterManager.getProjectCluster.nodes.toSet
            }

            // reset root cluster
            cachedClusterManager.getProjectCluster.clearNodes()
            cachedClusterManager.getProjectCluster.clusterable = true

            // reset all states in the default cluster manager... (using reflections)
            val methods = classOf[de.tud.cs.st.clusters.framework.structure.util.DefaultClusterManager].getMethods

            methods.filter(_.getName == "clusterNodes") foreach { method ⇒
                method.setAccessible(true);
                val clusterNodes = method.invoke(cachedClusterManager).asInstanceOf[scala.collection.mutable.Map[Int, Cluster]]
                clusterNodes.clear()
                clusterNodes.put(cachedClusterManager.getProjectCluster.uniqueID, cachedClusterManager.getProjectCluster)
            }

            methods.filter(_.getName == "de$tud$cs$st$clusters$framework$structure$util$NodeStore$_setter_$clusterNodes_$eq") foreach { method ⇒
                method.setAccessible(true);
                method.invoke(cachedClusterManager, scala.collection.mutable.Map(cachedClusterManager.getProjectCluster.uniqueID -> cachedClusterManager.getProjectCluster).asInstanceOf[AnyRef])
            }

            methods.filter(_.getName == "de$tud$cs$st$clusters$framework$structure$util$ClusterFactory$$nextGloballyUniqueID_$eq") foreach { method ⇒
                method.setAccessible(true);
                method.invoke(cachedClusterManager, 0.asInstanceOf[AnyRef])
            }

            methods.filter(_.getName == "de$tud$cs$st$clusters$framework$structure$util$ClusterIDsMap$_setter_$de$tud$cs$st$clusters$framework$structure$util$ClusterIDsMap$$clusterIDs_$eq") foreach { method ⇒
                method.setAccessible(true);
                method.invoke(cachedClusterManager, scala.collection.mutable.WeakHashMap[String, Int]().asInstanceOf[AnyRef])
            }

            methods.filter(_.getName == "de$tud$cs$st$clusters$framework$structure$util$ClusterIDsMap$$nextClusterID_$eq") foreach { method ⇒
                method.setAccessible(true);
                method.invoke(cachedClusterManager, (cachedClusterManager.getProjectCluster.uniqueID + 1).asInstanceOf[AnyRef])
            }

            // add all source elements directly to the root cluster
            cachedSourceElements foreach { node ⇒
                node.metaInfo.empty
                node.clusterable = false
                cachedClusterManager.getProjectCluster.addNode(node)
            }

            cachedClusterManager
        }
    }
}

