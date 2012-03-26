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

import structure.Cluster
import structure.SourceElementNode
import java.io.File
import mojo.MoJoCalculator

/**
 * The MoJoWrapper wraps an external MoJo implementation and
 * adds a new Metric called MoJoHM that also considers the
 * cluster hierarchy while determine a similarity value.
 *
 * @author Thomas Schlosser
 *
 */
class MoJoWrapper(val calculatedCluster: Cluster, val authorativeCluster: Cluster) {

    var levelLimit: Option[Int] = None

    val maxDepthCalculatedCluster = calcMaxDepth(calculatedCluster)
    val maxDepthAuthorativeCluster = calcMaxDepth(authorativeCluster)
    val maxDepth: Int = scala.math.max(maxDepthCalculatedCluster, maxDepthAuthorativeCluster)

    def singleDirectionMoJoHM: Double = {
        var mojoHMValue = 1.0
        val limit = levelLimit.getOrElse(maxDepth)
        1 to limit foreach { currentDepth ⇒
            val sourceFile = writeIntoTempRSFfile(calculatedCluster, Some(currentDepth))
            val targetFile = writeIntoTempRSFfile(authorativeCluster, Some(currentDepth))
            val relFile = null

            val mjc = new MoJoCalculator(sourceFile, targetFile, relFile)
            mojoHMValue = mojoHMValue * (mjc.mojofm() / 100.0)
            removeFile(sourceFile)
            removeFile(targetFile)
        }
        mojoHMValue * 100.0
    }

    def singleDirectionMoJo: Long = {
        val sourceFile = writeIntoTempRSFfile(calculatedCluster, levelLimit)
        val targetFile = writeIntoTempRSFfile(authorativeCluster, levelLimit)
        val relFile = null

        val mjc = new MoJoCalculator(sourceFile, targetFile, relFile)
        val result = mjc.mojo()

        removeFile(sourceFile)
        removeFile(targetFile)

        result
    }

    def singleDirectionMoJoPlus: Long = {
        val sourceFile = writeIntoTempRSFfile(calculatedCluster, levelLimit)
        val targetFile = writeIntoTempRSFfile(authorativeCluster, levelLimit)
        val relFile = null

        val mjc = new MoJoCalculator(sourceFile, targetFile, relFile)
        val result = mjc.mojoplus()

        removeFile(sourceFile)
        removeFile(targetFile)

        result
    }

    def singleDirectionMoJoFM: Double = {
        val sourceFile = writeIntoTempRSFfile(calculatedCluster, levelLimit)
        val targetFile = writeIntoTempRSFfile(authorativeCluster, levelLimit)
        val relFile = null

        val mjc = new MoJoCalculator(sourceFile, targetFile, relFile)
        val result = mjc.mojofm()

        removeFile(sourceFile)
        removeFile(targetFile)

        result
    }

    def doubleDirectionMoJo: Long = {
        val sourceFile = writeIntoTempRSFfile(calculatedCluster, levelLimit)
        val targetFile = writeIntoTempRSFfile(authorativeCluster, levelLimit)
        val relFile = null

        var mjc = new MoJoCalculator(sourceFile, targetFile, relFile)
        val one = mjc.mojo()
        mjc = new MoJoCalculator(targetFile, sourceFile, relFile)
        val two = mjc.mojo()

        removeFile(sourceFile)
        removeFile(targetFile)

        scala.math.min(one, two)
    }

    def doubleDirectionMoJoPlus: Long = {
        val sourceFile = writeIntoTempRSFfile(calculatedCluster, levelLimit)
        val targetFile = writeIntoTempRSFfile(authorativeCluster, levelLimit)
        val relFile = null

        var mjc = new MoJoCalculator(sourceFile, targetFile, relFile)
        val one = mjc.mojoplus()
        mjc = new MoJoCalculator(targetFile, sourceFile, relFile)
        val two = mjc.mojoplus()

        removeFile(sourceFile)
        removeFile(targetFile)

        scala.math.min(one, two)
    }

    def doubleDirectionMojoFM: Double = {
        val sourceFile = writeIntoTempRSFfile(calculatedCluster, levelLimit)
        val targetFile = writeIntoTempRSFfile(authorativeCluster, levelLimit)
        val relFile = null

        var mjc = new MoJoCalculator(sourceFile, targetFile, relFile)
        val one = mjc.mojofm()
        mjc = new MoJoCalculator(targetFile, sourceFile, relFile)
        val two = mjc.mojofm()

        removeFile(sourceFile)
        removeFile(targetFile)

        scala.math.max(one, two)
    }

    /**
     *
     * asks for the sequence of Move and Join operations (single
     * direction)
     * @return
     */
    def showMoveJoinSequence() = {
        val sourceFile = writeIntoTempRSFfile(calculatedCluster, levelLimit)
        val targetFile = writeIntoTempRSFfile(authorativeCluster, levelLimit)
        val relFile = null

        val mjc = new MoJoCalculator(sourceFile, targetFile, relFile)
        mjc.showSequence()

        removeFile(sourceFile)
        removeFile(targetFile)
    }

    private def writeIntoTempRSFfile(cluster: Cluster, levelLimit: Option[Int]): String = {
        val tmpFile = File.createTempFile(cluster.uniqueID+"_", ".tmp")
        var data: List[String] = Nil

        def extractRSFContentFromCluster(cl: Cluster, containerID: Int, currentDepth: Int = 1) {
            cl.children foreach {
                case subCluster: Cluster ⇒ {
                    if (levelLimit.isEmpty || currentDepth < levelLimit.get) {
                        extractRSFContentFromCluster(subCluster, subCluster.uniqueID, currentDepth + 1)
                    }
                    else {
                        extractRSFContentFromCluster(subCluster, containerID, currentDepth + 1)
                    }
                }
                case sen: SourceElementNode ⇒
                    data = "contain "+containerID+" "+sen.uniqueID :: data
            }
        }

        extractRSFContentFromCluster(cluster, cluster.uniqueID)

        printToFile(tmpFile)(p ⇒ {
            data.foreach(p.println)
        })

        tmpFile.getAbsolutePath()
    }

    private def removeFile(filePath: String) {
        new File(filePath).delete()
    }

    private def printToFile(f: java.io.File)(op: java.io.PrintWriter ⇒ Unit) {
        val p = new java.io.PrintWriter(f)
        try { op(p) } finally { p.close() }
    }

    private def calcMaxDepth(cluster: Cluster): Int = {
        var maxDpth = 0
        cluster.children foreach {
            case subCl: Cluster ⇒
                maxDpth = scala.math.max(maxDpth, calcMaxDepth(subCl))
            case sen: SourceElementNode ⇒
        }
        return maxDpth + 1
    }
}
