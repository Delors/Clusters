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

/**
 * @author Thomas Schlosser
 *
 */
class QualityTest extends AbstractEvaluationTest {

    protected def evaluate(
        testName: String,
        sourceFiles: SourceFile,
        referenceClusteringFilePath: String,
        testRuns: Int,
        measuredRuns: Int,
        allStageCombos: Array[(String, Array[ClusteringStage])]) {

        println(testName+" - START")

        if (referenceClusteringFilePath == null)
            sys.error("No reference clustering file is given!")

        val referenceClusters = ReferenceClusterCreator.readReferenceCluster(
            sourceFiles,
            new java.io.File(referenceClusteringFilePath))
        //        val maxLevelReferenceCluster = maxDepth(referenceClusters)
        //        println("Levels (ReferenceCluster):"+maxLevelReferenceCluster)

        //LaTeX output:
        println("""%TODO: use correct ylabel MoJoFM and MoJoHM, respectively.
\begin{figure}[htp]
	\centering
	\begin{tikzpicture}
		\pgfplotsset{width=0.5\textwidth}
		\begin{axis}[
			xtick=data,
			xlabel=level,
			ylabel=MojoHM value,
			cycle list name= black white,
			legend style={at={(-0.3,0.5)},anchor=east,legend}
		]""")

        var i = 1
        allStageCombos foreach { combo ⇒
            val (comboName, stageCombo) = combo
            // println("run combination #"+i+" ("+comboName+")")

            var clusteringPipeline: ClusteringPipeline =
                new ClusteringPipeline(stageCombo, None)

            val extractedCluster = clusteringPipeline.runPipeline(sourceFiles)

            var mjw = new MoJoWrapper(extractedCluster, referenceClusters)

            val maxLevelReferenceCluster = mjw.maxDepthAuthorativeCluster
            val maxLevelExtractedCluster = mjw.maxDepthCalculatedCluster
            // println("Levels (ExctractedCluster):"+maxLevelExtractedCluster)
            val levelLimit = scala.math.max(maxLevelReferenceCluster, maxLevelExtractedCluster)
            // LaTeX output:
            println("\\addlegendentry{"+comboName+"}")
            println("\\addplot coordinates {")
            1 to levelLimit foreach { level ⇒
                mjw.levelLimit = Some(level)
                //                println("MoJo(level="+level+"):"+mjw.singleDirectionMoJo+"\t MojoPlus: "+mjw.singleDirectionMoJoPlus+"\t MojoFm: "+mjw.singleDirectionMoJoFM)
                //                println("MoJoFM(level="+level+"):"+mjw.singleDirectionMoJoFM)
                //LaTeX format:
                println("("+level+","+mjw.singleDirectionMoJoHM+")")
            }
            // LaTeX output:
            println("};")
            i += 1
        }

        //LaTeX output:
        println("""		\end{axis}
    	\end{tikzpicture}
    \caption{"""+testName+"""} %TODO: change caption
    \label{fig:TODO} %TODO: change label
\end{figure}""")

        println(testName+" - END\n")
    }
}

