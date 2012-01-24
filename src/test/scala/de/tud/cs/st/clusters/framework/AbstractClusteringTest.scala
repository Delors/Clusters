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
import pipeline.ClusteringStage
import pipeline.ClusteringPipeline
import pipeline.DefaultClusteringPipeline
import structure.Cluster
import structure.util.ClusterManager
import _root_.de.tud.cs.st.bat.resolved.ClassFile
import _root_.de.tud.cs.st.bat.resolved.reader.Java6Framework
import _root_.de.tud.cs.st.bat.resolved.dependency.DependencyExtractor
import _root_.de.tud.cs.st.util.perf.PerformanceEvaluation
import pipeline.ClusteringResultWriter
import pipeline.DOTClusteringResultWriter
import pipeline.GMLClusteringResultWriter
import pipeline.GraphmlClusteringResultWriter
import pipeline.GraphmlClusteringResultWriterConfiguration

/**
 * @author Thomas Schlosser
 *
 */
trait AbstractClusteringTest extends FunSuite
        with PerformanceEvaluation {

    type BaseDependencyExtractor = ClusterManager

    protected def testClustering(testName: String,
                                 extractDependencies: (DependencyExtractor) ⇒ Unit,
                                 outputFileName: Option[String] = None,
                                 includeSingleNodes: Boolean = true,
                                 includeEdges: Boolean = true)(implicit clusteringStages: Array[ClusteringStage[_]]): Cluster = {
        println(testName+" - START")

        var clusteringPipeline: ClusteringPipeline = null
        if (outputFileName.isDefined) {
            //            clusteringPipeline = ClusteringPipeline(
            //                clusteringStages,
            //                extractDependencies,
            //                nodeStore ⇒ new DOTClusteringResultWriter(
            //                    outputFileName.get,
            //                    includeSingleNodes,
            //                    includeEdges))

            //            clusteringPipeline = ClusteringPipeline(
            //                clusteringStages,
            //                extractDependencies,
            //                nodeStore ⇒ new GMLClusteringResultWriter(outputFileName.get))

            val writerConfiguration = new {
                override val aggregateEdges = true
                override val showEdgeLabels = false
                override val showSourceElementNodes = false
            } with GraphmlClusteringResultWriterConfiguration
            clusteringPipeline = new DefaultClusteringPipeline(
                clusteringStages,
                extractDependencies,
                nodeStore ⇒ new GraphmlClusteringResultWriter(outputFileName.get, nodeStore, writerConfiguration))
        }
        else {
            clusteringPipeline = new DefaultClusteringPipeline(clusteringStages, extractDependencies)
        }
        //TODO add pipeline configuration

        val cluster = clusteringPipeline.runPipeline(true)

        println(testName+" - END")
        cluster
    }

    protected def testDependencyExtraction(testName: String,
                                           extractDependencies: (DependencyExtractor) ⇒ Unit,
                                           dotFileName: Option[String] = None,
                                           includeSingleNodes: Boolean = true,
                                           includeEdges: Boolean = true) {
        testClustering(testName, extractDependencies, dotFileName, includeSingleNodes, includeSingleNodes)(null)
    }

    protected def extractDependencies(zipFile: String, classFiles: String*): (DependencyExtractor) ⇒ Unit = {
        dependencyExtractor ⇒ for (classFile ← classFiles) dependencyExtractor.process(Java6Framework.ClassFile(zipFile, classFile))
    }

    protected def extractDependencies(zipFile: String): (DependencyExtractor) ⇒ Unit = {
        dependencyExtractor ⇒ for (cf ← Java6Framework.ClassFiles(zipFile)) dependencyExtractor.process(cf)
    }

    protected val clusteringTestProjectDependencyExtractor = extractDependencies("test/classfiles/ClusteringTestProject.zip")
    protected val getterSetterTestClassDependencyExtractor = extractDependencies("test/classfiles/ClusteringTestProject.zip", "test/GetterSetterTestClass.class")
    protected val stronglyConnectedComponentsTestClassDependencyExtractor = extractDependencies("test/classfiles/ClusteringTestProject.zip", "test/StronglyConnectedComponentsTestClass.class")

    protected val hibernateDependencyExtractor = extractDependencies("test/classfiles/hibernate-core-3.6.0.Final.jar")

    protected val cocomeDependencyExtractor = extractDependencies("test/classfiles/cocome-impl-classes.jar")
    protected val cocomePrintercontrollerDependencyExtractor = extractDependencies("test/classfiles/cocome-impl-classes.jar",
        "org/cocome/tradingsystem/cashdeskline/cashdesk/printercontroller/PrinterControllerEventHandlerIf.class",
        "org/cocome/tradingsystem/cashdeskline/cashdesk/printercontroller/impl/PrinterController.class",
        "org/cocome/tradingsystem/cashdeskline/cashdesk/printercontroller/impl/PrinterControllerEventHandlerImpl.class",
        "org/cocome/tradingsystem/cashdeskline/cashdesk/printercontroller/impl/PrinterStates.class")
}