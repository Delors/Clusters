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
import framework.TestSources._
import framework.TestResultWriterCreators._
import framework.pipeline.ClusteringStage
import algorithm.ApplicationLibrariesSeparator
import algorithm.ApplicationLibrariesSeparatorConfiguration
import algorithm.PackageClustering
import algorithm.PackageClusteringConfiguration
import algorithm.ImplementationTestingSeparator
import algorithm.ImplementationTestingSeparatorConfiguration
import algorithm.GetterSetterClustering
import algorithm.GetterSetterClusteringConfiguration
import algorithm.ChineseWhispers
import algorithm.ChineseWhispersConfiguration
import algorithm.StronglyConnectedComponentsClustering
import algorithm.StronglyConnectedComponentsClusteringConfiguration
import strategy.FirstClusterablesClusteringStrategy
import strategy.FixedPointIterationClusteringStrategy
import strategy.IdentifierBasedClusteringStrategy
import strategy.MinClusterSizeClusteringStrategy
import framework.util.ReferenceClusterCreator

/**
 * @author Thomas Schlosser
 *
 */
@RunWith(classOf[JUnitRunner])
class CombinedClusteringTest extends AbstractClusteringTest {

    val appLibsConfig = new ApplicationLibrariesSeparatorConfiguration {}
    val pkgConfig = new PackageClusteringConfiguration {}
    val implTestConfig = new ImplementationTestingSeparatorConfiguration {}
    val getterSetterConfig = new GetterSetterClusteringConfiguration {}
    val chineseWhispersConfig = new ChineseWhispersConfiguration {}
    val sccConfig = new StronglyConnectedComponentsClusteringConfiguration {}

    val appLibsSeparator = new ApplicationLibrariesSeparator(appLibsConfig)
    val packageClustering = new {
        val clusterIdentifier = appLibsConfig.librariesClusterIdentifier
    } with PackageClustering(pkgConfig) with IdentifierBasedClusteringStrategy
    val implTestSeparator = new ImplementationTestingSeparator(implTestConfig) with FirstClusterablesClusteringStrategy
    val sccClustering = new StronglyConnectedComponentsClustering(sccConfig) with FirstClusterablesClusteringStrategy
    val getterSetterClustering = new GetterSetterClustering(getterSetterConfig) with FirstClusterablesClusteringStrategy
    val chineseWhispers = new {
        override val minClusterSizeThreshold: Int = 3
    } with ChineseWhispers(chineseWhispersConfig) with MinClusterSizeClusteringStrategy with FirstClusterablesClusteringStrategy with FixedPointIterationClusteringStrategy

    implicit val clusteringStages: Array[ClusteringStage] = Array(
        appLibsSeparator,
        packageClustering,
        implTestSeparator,
        sccClustering,
        getterSetterClustering,
        chineseWhispers
    )

    test("testCombinedClustering [ClusteringTestProject]") {
        testClustering(
            "testCombinedClustering [ClusteringTestProject]",
            graphmlClusteringResultWriterCreator("combinedClust_ClusteringTestProject"),
            clusteringTestProjectSourceZipFile)
    }

    test("testCombinedClustering [Flashcards]") {
        testClustering(
            "testCombinedClustering [Flashcards]",
            graphmlClusteringResultWriterCreator("combinedClust_flashcards"),
            flashcardsSourceZipFile)
    }

    test("testCombinedClustering [cocome]") {
        testClustering(
            "testCombinedClustering [cocome]",
            graphmlClusteringResultWriterCreator("combinedClust_cocome", _maxNumberOfLevels = Some(4)),
            cocomeSourceZipFile)
    }

    test("testCombinedClustering [hibernate]") {
        testClustering(
            "testCombinedClustering [hibernate]",
            graphmlClusteringResultWriterCreator("combinedClust_hibernate"),
            hibernateSourceZipFile)
    }

    test("testCombinedClustering [javaRuntime]") {
        testClustering(
            "testCombinedClustering [javaRuntime]",
            javaRuntimeSourceZipFile)
    }

}
