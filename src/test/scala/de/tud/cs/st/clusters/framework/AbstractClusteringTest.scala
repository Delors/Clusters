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

import java.io.File
import java.io.FileWriter
import java.util.zip.ZipFile
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import pipeline.Clustering
import structure.Cluster
import structure.util.ClusterBuilder
import _root_.de.tud.cs.st.bat.resolved.ClassFile
import _root_.de.tud.cs.st.bat.resolved.reader.Java6Framework
import _root_.de.tud.cs.st.bat.resolved.dependency.DependencyExtractor
import _root_.de.tud.cs.st.util.perf.PerformanceEvaluation
import de.tud.cs.st.bat.resolved.dependency.FilterDependenciesToBaseAndVoidTypes
import de.tud.cs.st.bat.resolved.ClassFileTestUtility

/**
 * @author Thomas Schlosser
 *
 */
trait AbstractClusteringTest extends FunSuite
        with ClassFileTestUtility
        with PerformanceEvaluation {

    protected def testClustering(testName: String,
                                 extractDependencies: (DependencyExtractor) ⇒ Unit,
                                 dotFileName: Option[String] = None,
                                 includeSingleNodes: Boolean = true,
                                 includeEdges: Boolean = true)(implicit clustering: ClusterBuilder ⇒ Clustering) {
        println(testName+" - START")

        val clusterBuilder = new ClusterBuilder with FilterDependenciesToBaseAndVoidTypes
        val dependencyExtractor = new DependencyExtractor(clusterBuilder)

        time(duration ⇒ println("time to read classfiles and extract dependencies: "+nanoSecondsToMilliseconds(duration)+"ms")) {
            extractDependencies(dependencyExtractor)
        }

        println("numberOfNode:"+clusterBuilder.getRootCluster.getNodes.size)
        var clusters: Array[Cluster] = null
        if (clustering != null) {
            time(duration ⇒ println("time to cluster input: "+nanoSecondsToMilliseconds(duration)+"ms")) {
                clusters = clustering(clusterBuilder).process(Array(clusterBuilder.getRootCluster))
            }
        }
        else {
            clusters = Array(clusterBuilder.getRootCluster)
        }
        if (dotFileName.isDefined) {
            clusters.foreach(c ⇒ {
                println("write cluster["+c.identifier+"] into dot file["+dotFileName.get+"_"+c.identifier+"]")
                val fw = new FileWriter(dotFileName.get+"_"+c.identifier+".dot")
                fw.write(c.toDot(includeSingleNodes, includeEdges))
                fw.close()
            })
        }

        println(testName+" - END")
    }

    protected def testDependencyExtraction(testName: String,
                                           extractDependencies: (DependencyExtractor) ⇒ Unit,
                                           dotFileName: Option[String] = None,
                                           includeSingleNodes: Boolean = true,
                                           includeEdges: Boolean = true) {
        testClustering(testName, extractDependencies, dotFileName, includeSingleNodes, includeSingleNodes)(null)
    }

    protected def extractDependencies(zipFile: String, classFile: String): (DependencyExtractor) ⇒ Unit = {
        dependencyExtractor ⇒ dependencyExtractor.process(Java6Framework.ClassFile(zipFile, classFile))
    }

    protected def extractDependencies(zipFile: String): (DependencyExtractor) ⇒ Unit = {
        dependencyExtractor ⇒ for (cf ← ClassFiles(zipFile)) dependencyExtractor.process(cf)
    }
}