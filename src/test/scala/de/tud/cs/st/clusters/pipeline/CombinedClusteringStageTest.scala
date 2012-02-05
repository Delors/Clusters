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
package pipeline

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import framework.AbstractClusteringTest
import framework.pipeline.ClusteringStage
import algorithm.ApplicationLibrariesSeparatorStage
import algorithm.ApplicationLibrariesSeparatorStageConfiguration
import algorithm.PackageClusteringStage
import algorithm.PackageClusteringAlgorithmConfiguration
import algorithm.ImplementationTestingSeparatorStage
import algorithm.ImplementationTestingSeparatorStageConfiguration
import algorithm.GetterSetterClusteringStage
import algorithm.GetterSetterClusteringAlgorithmConfiguration
import algorithm.SimilarityMetricClusteringStage
import algorithm.SimilarityMetricClusteringAlgorithmConfiguration
import algorithm.StronglyConnectedComponentsClusteringStage
import algorithm.StronglyConnectedComponentsClusteringAlgorithmConfiguration
import strategy.FirstClusterablesClusteringStrategy
import strategy.FixedPointIterationClusteringStrategy
import strategy.IdentifierBasedClusteringStrategy
import strategy.MinClusterSizeClusteringStrategy
import framework.util.ReferenceClusterCreator
import framework.validation.MoJoWrapper

/**
 * @author Thomas Schlosser
 *
 */
@RunWith(classOf[JUnitRunner])
class CombinedClusteringStageTest extends AbstractClusteringTest {

    val appLibConfiguration = new ApplicationLibrariesSeparatorStageConfiguration {}
    val pkgConfiguration = new PackageClusteringAlgorithmConfiguration {}
    val implTestConfiguration = new ImplementationTestingSeparatorStageConfiguration {}
    val getterSetterConfiguration = new GetterSetterClusteringAlgorithmConfiguration {}
    val similarityMetricConfiguration = new SimilarityMetricClusteringAlgorithmConfiguration {}
    val sccConfiguration = new StronglyConnectedComponentsClusteringAlgorithmConfiguration {}

    //TODO: implement more strategies and make use of the metaInfo data;

    val libStage = new ApplicationLibrariesSeparatorStage(appLibConfiguration)
    val pkgStage = new {
        val clusterIdentifier = "libraries"
    } with PackageClusteringStage(pkgConfiguration) with IdentifierBasedClusteringStrategy
    val implTestStage = new ImplementationTestingSeparatorStage(implTestConfiguration) with FirstClusterablesClusteringStrategy
    val sccStage = new StronglyConnectedComponentsClusteringStage(sccConfiguration) with FirstClusterablesClusteringStrategy
    val getterSetterStage = new GetterSetterClusteringStage(getterSetterConfiguration) with FirstClusterablesClusteringStrategy
    val simMetricStage = new {
        override val minClusterSizeThreshold: Int = 3
    } with SimilarityMetricClusteringStage(similarityMetricConfiguration) with MinClusterSizeClusteringStrategy with FirstClusterablesClusteringStrategy with FixedPointIterationClusteringStrategy

    implicit val clusteringStages: Array[ClusteringStage] = Array(
        libStage,
        pkgStage,
        implTestStage,
        sccStage,
        getterSetterStage,
        simMetricStage
    )

    test("testCombinedClusteringStage [ClusteringTestProject]") {
        val extractedCluster = testClustering(
            "testCombinedClusteringStage [ClusteringTestProject]",
            clusteringTestProjectDependencyExtractor,
            graphmlClusteringResultWriterCreator("combinedClust_ClusteringTestProject"))

        val referenceClusters = ReferenceClusterCreator.readReferenceCluster(
            "test/classfiles/ClusteringTestProject.zip",
            new java.io.File("test/referenceCluster/ClusteringTestProject.sei"))

        println("MoJo:")
        var mjw = new MoJoWrapper(referenceClusters, extractedCluster)
        println(mjw.doubleDirectionMojoFM)
    }

    //    test("testCombinedClusteringStage [cocome]") {
    //        testClustering(
    //            "testCombinedClusteringStage [cocome]",
    //            cocomeDependencyExtractor,
    //            graphmlClusteringResultWriterCreator("combinedClust_cocome", _maxNumberOfLevels = Some(4)))
    //    }
    //
    //    test("testCombinedClusteringStage [hibernate]") {
    //        testClustering(
    //            "testCombinedClusteringStage [hibernate]",
    //            hibernateDependencyExtractor,
    //            graphmlClusteringResultWriterCreator("combinedClust_hibernate"))
    //    }

}
