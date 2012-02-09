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
package validation

import structure.Cluster
import structure.SourceElementNode
import java.io.File
import mojo.MoJoCalculator

/**
 * @author Thomas Schlosser
 *
 */
class MoJoWrapper(val authorativeCluster: Cluster, val calculatedCluster: Cluster) {

    def singleDirectionMoJoPlus: Double = {
        val sourceFile = writeIntoTempRSFfile(authorativeCluster)
        val targetFile = writeIntoTempRSFfile(calculatedCluster)
        val relFile = null

        val mjc = new MoJoCalculator(sourceFile, targetFile, relFile)
        mjc.mojoplus()
    }

    def doubleDirectionMoJoPlus: Double = {
        val sourceFile = writeIntoTempRSFfile(authorativeCluster)
        val targetFile = writeIntoTempRSFfile(calculatedCluster)
        val relFile = null

        var mjc = new MoJoCalculator(sourceFile, targetFile, relFile)
        val one = mjc.mojoplus()
        mjc = new MoJoCalculator(targetFile, sourceFile, relFile)
        val two = mjc.mojoplus()
        Math.min(one, two)
    }

    def doubleDirectionMoJo: Double = {
        val sourceFile = writeIntoTempRSFfile(authorativeCluster)
        val targetFile = writeIntoTempRSFfile(calculatedCluster)
        val relFile = null

        var mjc = new MoJoCalculator(sourceFile, targetFile, relFile)
        val one = mjc.mojo()
        mjc = new MoJoCalculator(targetFile, sourceFile, relFile)
        val two = mjc.mojo()
        Math.min(one, two)
    }

    def singleDirectionMoJoFM: Double = {
        val sourceFile = writeIntoTempRSFfile(authorativeCluster)
        val targetFile = writeIntoTempRSFfile(calculatedCluster)
        val relFile = null

        val mjc = new MoJoCalculator(sourceFile, targetFile, relFile)
        mjc.mojofm()
    }

    def doubleDirectionMojoFM: Double = {
        val sourceFile = writeIntoTempRSFfile(authorativeCluster)
        val targetFile = writeIntoTempRSFfile(calculatedCluster)
        val relFile = null

        var mjc = new MoJoCalculator(sourceFile, targetFile, relFile)
        val one = mjc.mojofm()
        mjc = new MoJoCalculator(targetFile, sourceFile, relFile)
        val two = mjc.mojofm()
        Math.max(one, two)
    }

    /**
     *
     * asks for the sequence of Move and Join operations (single
     * direction)
     * @return
     */
    def showMoveJoinSequence() = {
        val sourceFile = writeIntoTempRSFfile(authorativeCluster)
        val targetFile = writeIntoTempRSFfile(calculatedCluster)
        val relFile = null

        val mjc = new MoJoCalculator(sourceFile, targetFile, relFile)
        mjc.showSequence()
    }

    private def writeIntoTempRSFfile(cluster: Cluster): String = {
        val tmpFile = File.createTempFile(cluster.uniqueID+"_", ".tmp")
        var data: List[String] = Nil

        def extractRSFContentFromCluster(cl: Cluster) {
            cl.nodes foreach {
                case subCluster: Cluster ⇒
                    extractRSFContentFromCluster(subCluster)
                case sen: SourceElementNode ⇒
                    data = "contain "+cl.uniqueID+" "+sen.uniqueID :: data
            }
        }

        extractRSFContentFromCluster(cluster)

        printToFile(tmpFile)(p ⇒ {
            data.foreach(p.println)
        })

        tmpFile.getAbsolutePath()
    }

    private def printToFile(f: java.io.File)(op: java.io.PrintWriter ⇒ Unit) {
        val p = new java.io.PrintWriter(f)
        try { op(p) } finally { p.close() }
    }
}
