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

    var comboNames: Array[String] = _
    var maxLevelReferenceClustering: Int = -1

    // comboName => (maxLevelExtractedCluster,Array[(level,MoJoXXValue)])
    var resultsMoJoFM: scala.collection.mutable.Map[String, (Int, Array[(Int, Double)])] = _
    var resultsMoJoHM: scala.collection.mutable.Map[String, (Int, Array[(Int, Double)])] = _

    protected override def beforeEvaluation(testName: String, projectName: String, referenceCluster: Cluster) {
        super.beforeEvaluation(testName, projectName, referenceCluster)
        comboNames = Array.empty
        maxLevelReferenceClustering = -1
        resultsMoJoFM = scala.collection.mutable.Map.empty
        resultsMoJoHM = scala.collection.mutable.Map.empty
    }

    protected override def beforeStageEvaluation(comboNumber: Int, testName: String, projectName: String, comboName: String) {
        super.beforeStageEvaluation(comboNumber, testName, projectName, comboName)
    }

    protected override def afterStageEvaluation(comboNumber: Int, testName: String, projectName: String, comboName: String, mjw: MoJoWrapper, extractedCluster: Cluster, maxLevelReferenceCluster: Int, maxLevelExtractedCluster: Int, levelLimit: Int) {
        comboNames = comboNames :+ comboName
        maxLevelReferenceClustering = maxLevelReferenceCluster
        1 to levelLimit foreach { level ⇒
            mjw.levelLimit = Some(level)
            val mojoHMvalue = mjw.singleDirectionMoJoHM
            var combo: (Int, Array[(Int, Double)]) = resultsMoJoHM.getOrElse(comboName, (maxLevelExtractedCluster, Array.empty))
            combo = (combo._1, combo._2 :+ ((level, mojoHMvalue)))
            resultsMoJoHM(comboName) = combo
            val mojoFMvalue = mjw.singleDirectionMoJoFM
            combo = resultsMoJoFM.getOrElse(comboName, (maxLevelExtractedCluster, Array.empty))
            combo = (combo._1, combo._2 :+ ((level, mojoFMvalue)))
            resultsMoJoFM(comboName) = combo
        }
        super.afterStageEvaluation(comboNumber, testName, projectName, comboName, mjw, extractedCluster, maxLevelReferenceCluster, maxLevelExtractedCluster, levelLimit)
    }

    protected override def afterEvaluation(testName: String, projectName: String) {
        writeFigure(projectName, mojoFM, resultsMoJoFM)
        writeFigure(projectName, mojoHM, resultsMoJoHM)
        writeTable(projectName, mojoFM, resultsMoJoFM)
        writeTable(projectName, mojoHM, resultsMoJoHM)

        super.afterEvaluation(testName, projectName)
    }

    protected def writeFigure(projectName: String, mojoVariant: String, results: scala.collection.mutable.Map[String, (Int, Array[(Int, Double)])]) {
        val latexFigureFileWriter = new java.io.FileWriter(new java.io.File(projectName+"_"+mojoVariant+".tex"))
        try {
            latexFigureFileWriter.write("""\begin{figure}[htp]
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
            0 to comboNames.size - 1 foreach { i ⇒
                val comboName = comboNames(i)
                latexFigureFileWriter.write("\\addlegendentry{$\\"+comboName+"$}\n")
                latexFigureFileWriter.write("\\addplot coordinates {\n")
                val comboResults = results(comboName)
                comboResults._2.foreach { comboRes ⇒
                    val (level, mojoXXValue) = comboRes
                    latexFigureFileWriter.write("("+level+","+mojoXXValue+")\n")
                }

                latexFigureFileWriter.write("};\n")
            }

            latexFigureFileWriter.write("""		\end{axis}
    	\end{tikzpicture}
    \caption{"""+mojoVariant+" values of "+projectName+"""} %TODO: change caption
    \label{fig:"""+projectName+"_"+mojoVariant+"""}
\end{figure}""")
        }
        finally {
            latexFigureFileWriter.close()
        }
    }

    protected def writeTable(projectName: String, mojoVariant: String, results: scala.collection.mutable.Map[String, (Int, Array[(Int, Double)])]) {
        val maxLevels = results.values.foldLeft(0)((c, a) ⇒ scala.math.max(c, a._2.size))
        val latexTableFileWriter = new java.io.FileWriter(new java.io.File(projectName+"_"+mojoVariant+"_table.tex"))
        try {
            latexTableFileWriter.write("""\begin{table}[H]
\centering
\begin{tabular}{>{\centering\arraybackslash}m{\widthof{Configuration}}""")
            1 to maxLevels foreach { _ ⇒
                latexTableFileWriter.write("|c")
            }
            latexTableFileWriter.write("""}
\toprule
\textbf{Pipeline\newline Configuration}""")
            1 to maxLevels foreach { l ⇒
                latexTableFileWriter.write(" & \\textbf{Level "+l)
                if (l == maxLevelReferenceClustering) {
                    latexTableFileWriter.write("$^{\\ast}$")
                }
                latexTableFileWriter.write("}")
            }
            latexTableFileWriter.write(" \\\\ \\midrule\n")

            0 to comboNames.size - 1 foreach { i ⇒
                val comboName = comboNames(i)
                latexTableFileWriter.write("\t$\\"+comboName+"$")
                val comboResults = results(comboName)
                0 to maxLevels - 1 foreach { l ⇒
                    if (comboResults._2.size <= l) {
                        latexTableFileWriter.write(" & --")
                    }
                    else {
                        val (level, mojoXXValue) = comboResults._2(l)
                        val mojoXXValueString = new java.text.DecimalFormat("#.##").format(mojoXXValue)
                        latexTableFileWriter.write(" & $"+mojoXXValueString+"\\%$")
                        if (level == comboResults._1) {
                            latexTableFileWriter.write("$^{\\dagger}$")
                        }
                    }
                }
                if (i < comboNames.size - 1) {
                    latexTableFileWriter.write(""" \\ % entry"""+"\n") //""" \\ \midrule % entry"""+"\n")
                }
                else {
                    latexTableFileWriter.write(""" \\ \bottomrule % entry"""+"\n")
                }
            }

            latexTableFileWriter.write("""\end{tabular}
\caption["""+mojoVariant+" values of experiment TODO -- "+projectName+"""] %TODO: fix name and experiment number!
{"""+mojoVariant+" values of experiment TODO -- "+projectName+""". %TODO: fix name and experiment number!
The abbreviations of the pipeline configurations are explained in Table~\ref{tab:evaluation:overviewPipelineConfiguration}.
The maximum level of the reference clustering is tagged by $\ast$ -- in the table header.
The """+mojoVariant+""" value of the generated clustering's maximum level is tagged by $\dagger$.}
\label{tab:"""+projectName+"_"+mojoVariant+"""}
\end{table}""")
        }
        finally {
            latexTableFileWriter.close()
        }
    }
}
