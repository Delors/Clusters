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
 * Note: This test class is not thread-safe.
 *
 * @author Thomas Schlosser
 *
 */
class LaTeXProducingQualityTest extends QualityTest {

    var latexFileWriter: java.io.FileWriter = null

    protected override def beforeEvaluation(testName: String, projectName: String) {
        super.beforeEvaluation(testName, projectName)
        latexFileWriter = new java.io.FileWriter(new java.io.File(projectName+"_"+mojoVariant+".tex"))
        latexFileWriter.write("""\begin{figure}[htp]
	\centering
	\begin{tikzpicture}
		\pgfplotsset{width=0.5\textwidth}
		\begin{axis}[
			xtick=data,
			xlabel=level,
			ylabel="""+mojoVariant+""" value,
			cycle list name= black white,
			legend style={at={(-0.3,0.5)},anchor=east,legend}
		]
""")
    }

    protected override def beforeStageEvaluation(comboNumber: Int, testName: String, projectName: String, comboName: String) {
        super.beforeStageEvaluation(comboNumber, testName, projectName, comboName)
    }

    protected override def afterStageEvaluation(comboNumber: Int, testName: String, projectName: String, comboName: String, mjw: MoJoWrapper, extractedCluster: Cluster, maxLevelReferenceCluster: Int, maxLevelExtractedCluster: Int, levelLimit: Int) {
        if (latexFileWriter != null) {
            latexFileWriter.write("\\addlegendentry{"+comboName+"}\n")
            latexFileWriter.write("\\addplot coordinates {\n")
            1 to levelLimit foreach { level ⇒
                mjw.levelLimit = Some(level)
                if (mojoVariant == mojoHM) {
                    latexFileWriter.write("("+level+","+mjw.singleDirectionMoJoHM+")\n")
                }
                else if (mojoVariant == mojoFM) {
                    latexFileWriter.write("("+level+","+mjw.singleDirectionMoJoFM+")\n")
                }
            }
            latexFileWriter.write("};\n")
        }
        afterStageEvaluation(comboNumber, testName, projectName, comboName, mjw, extractedCluster, maxLevelReferenceCluster, maxLevelExtractedCluster, levelLimit)
    }

    protected override def afterEvaluation(testName: String, projectName: String) {
        if (latexFileWriter != null) {
            try {
                latexFileWriter.write("""		\end{axis}
    	\end{tikzpicture}
    \caption{"""+mojoVariant+" values of "+projectName+"""} %TODO: change caption
    \label{fig:"""+projectName+"_"+mojoVariant+"""}
\end{figure}""")
            }
            finally {
                latexFileWriter.close()
            }
        }
        super.afterEvaluation(testName, projectName)
    }
}
