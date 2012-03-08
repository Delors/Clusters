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

import org.junit.runner.RunWith
import org.junit.Test
import org.scalatest.junit.JUnitRunner
import framework.AbstractClusteringTest
import framework.TestSources._
import framework.TestResultWriterCreators._
import framework.pipeline.ClusteringPipeline
import framework.pipeline.ClusteringStage
import de.tud.cs.st.clusters.pipeline.algorithm.ApplicationLibrariesSeparator
import de.tud.cs.st.clusters.pipeline.algorithm.ApplicationLibrariesSeparatorConfiguration
import de.tud.cs.st.clusters.pipeline.algorithm.ClassExtractor
import de.tud.cs.st.clusters.pipeline.algorithm.PackageClustering
import de.tud.cs.st.clusters.pipeline.algorithm.PackageClusteringConfiguration
import de.tud.cs.st.clusters.pipeline.algorithm.ImplementationTestingSeparator
import de.tud.cs.st.clusters.pipeline.algorithm.ImplementationTestingSeparatorConfiguration
import de.tud.cs.st.clusters.pipeline.algorithm.GetterSetterClustering
import de.tud.cs.st.clusters.pipeline.algorithm.GetterSetterClusteringConfiguration
import de.tud.cs.st.clusters.pipeline.algorithm.LayerClustering
import de.tud.cs.st.clusters.pipeline.algorithm.LayerClusteringConfiguration
import de.tud.cs.st.clusters.pipeline.algorithm.SimilarityMetricClustering
import de.tud.cs.st.clusters.pipeline.algorithm.SimilarityMetricClusteringConfiguration
import de.tud.cs.st.clusters.pipeline.algorithm.StronglyConnectedComponentsClustering
import de.tud.cs.st.clusters.pipeline.algorithm.StronglyConnectedComponentsClusteringConfiguration
import de.tud.cs.st.clusters.pipeline.strategy.FirstClusterablesClusteringStrategy
import de.tud.cs.st.clusters.pipeline.strategy.FixedPointIterationClusteringStrategy
import de.tud.cs.st.clusters.pipeline.strategy.IdentifierBasedClusteringStrategy
import de.tud.cs.st.clusters.pipeline.strategy.MinClusterSizeClusteringStrategy
import util.ReferenceClusterCreator
import util.SourceFile
import framework.pipeline.ClusteringResultWriter
import framework.structure.Cluster
import framework.structure.util.ClusterManager
import framework.structure.SourceElementNode
import de.tud.cs.st.util.perf._

/**
 * @author Thomas Schlosser
 *
 */
@RunWith(classOf[JUnitRunner])
abstract class AbstractEvaluationTest extends AbstractClusteringTest {

    val appLibsConfig = new ApplicationLibrariesSeparatorConfiguration {}
    val pkgConfig = new PackageClusteringConfiguration {}
    val implTestConfig = new ImplementationTestingSeparatorConfiguration {}
    val getterSetterConfig = new GetterSetterClusteringConfiguration {}
    val similarityMetricConfig = new SimilarityMetricClusteringConfiguration {}
    val sccConfig = new StronglyConnectedComponentsClusteringConfiguration {}
    val layerConfig = new LayerClusteringConfiguration {}

    val appLibsSeparator = new ApplicationLibrariesSeparator(appLibsConfig)
    val packageClustering = new {
        val clusterIdentifier = appLibsConfig.librariesClusterIdentifier
    } with PackageClustering(pkgConfig) with IdentifierBasedClusteringStrategy
    val implTestSeparator = new ImplementationTestingSeparator(implTestConfig) with FirstClusterablesClusteringStrategy
    val sccClustering = new StronglyConnectedComponentsClustering(sccConfig) with FirstClusterablesClusteringStrategy
    val getterSetterClustering = new GetterSetterClustering(getterSetterConfig) with FirstClusterablesClusteringStrategy
    val simMetricClustering = new {
        override val minClusterSizeThreshold: Int = 3
    } with SimilarityMetricClustering(similarityMetricConfig) with MinClusterSizeClusteringStrategy with FirstClusterablesClusteringStrategy with FixedPointIterationClusteringStrategy

    val layerClustering = new LayerClustering(layerConfig)
    val classExtractor = new ClassExtractor() with FirstClusterablesClusteringStrategy

    val onlySimMetricClustering: Array[ClusteringStage] = Array(simMetricClustering)
    val combinedStagesSimMetricClustering: Array[ClusteringStage] = Array(
        appLibsSeparator,
        packageClustering,
        implTestSeparator,
        sccClustering,
        getterSetterClustering,
        simMetricClustering
    )
    val onlyLayerClustering: Array[ClusteringStage] = Array(layerClustering)
    val combinedStagesLayerClustering: Array[ClusteringStage] = Array(
        appLibsSeparator,
        packageClustering,
        implTestSeparator,
        sccClustering,
        getterSetterClustering,
        layerClustering
    )
    val onlyClassExtractor: Array[ClusteringStage] = Array(classExtractor)
    val combinedStagesClassExtractor: Array[ClusteringStage] = Array(
        appLibsSeparator,
        packageClustering,
        implTestSeparator,
        sccClustering,
        getterSetterClustering,
        classExtractor
    )
    val combinedStagesWithoutFinalizer: Array[ClusteringStage] = Array(
        appLibsSeparator,
        packageClustering,
        implTestSeparator,
        sccClustering,
        getterSetterClustering
    )
    val onlyAppLibsSeparator: Array[ClusteringStage] = Array(appLibsSeparator)
    val onlyGetterSetterClustering: Array[ClusteringStage] = Array(getterSetterClustering)
    val onlyImplTestSeparator: Array[ClusteringStage] = Array(implTestSeparator)
    val onlyPackageClustering: Array[ClusteringStage] = Array(new PackageClustering(pkgConfig))
    val onlySCCClustering: Array[ClusteringStage] = Array(sccClustering)

    val allStageCombos: Array[(String, Array[ClusteringStage])] = Array(
        ("onlySimMetricClustering", onlySimMetricClustering),
        ("combinedStagesSimMetric", combinedStagesSimMetricClustering),
        ("onlyLayerClustering", onlyLayerClustering),
        ("combinedStagesLayerClustering", combinedStagesLayerClustering),
        ("onlyClassExtractor", onlyClassExtractor),
        ("combinedStagesClassExtractor", combinedStagesClassExtractor),
        ("combinedStagesWithoutFinalizer", combinedStagesWithoutFinalizer),
        ("onlyAppLibsSeparator", onlyAppLibsSeparator),
        //        ("onlyGetterSetterClustering", onlyGetterSetterClustering),
        //        ("onlyImplTestSeparator", onlyImplTestSeparator),
        ("onlyPackageClustering", onlyPackageClustering),
        //        ("onlySCCClustering", onlySCCClustering),
        ("noClustering", Array())
    )

    private def evaluate(
        testName: String,
        sourceFiles: SourceFile,
        referenceClusteringFilePath: String) {
        evaluate(testName, sourceFiles, referenceClusteringFilePath, allStageCombos)
    }

    protected def evaluate(
        testName: String,
        sourceFiles: SourceFile,
        referenceClusteringFilePath: String,
        allStageCombos: Array[(String, Array[ClusteringStage])])

    test("evaluate [Flashcards]") {
        evaluate(
            "evaluate [Flashcards]",
            flashcardsSourceZipFile,
            "test/referenceCluster/Flashcards 0.4 - target 1.6.sei")
    }

    // TODO: there are no valid reference clusters...
    //    test("evaluate [CoCoME]") {
    //        evaluate(
    //            "evaluate [CoCoME]",
    //            cocomeSourceZipFile,
    //            "test/referenceCluster/cocome-impl-classes.sei")
    //    }
    //
    //    test("evaluate [hibernate]") {
    //        evaluate(
    //            "evaluate [hibernate]",
    //            hibernateSourceZipFile,
    //            "test/referenceCluster/hibernate-core-3.6.0.Final.sei")
    //    }

    test("evaluate [ClusteringTestProject]") {
        evaluate(
            "evaluate [ClusteringTestProject]",
            clusteringTestProjectSourceZipFile,
            "test/referenceCluster/ClusteringTestProject.sei")
    }

    test("evaluate [clTestProjectexampleTest1SourcesZipFile]") {
        evaluate(
            "evaluate [clTestProjectexampleTest1SourcesZipFile]",
            clTestProjectExampleTest1SourcesZipFile,
            "test/referenceCluster/ClusteringTestProject-example-test1.sei")
    }

    test("evaluate [clTestProjectPatternAbstractFactorySourcesZipFile]") {
        evaluate(
            "evaluate [clTestProjectPatternAbstractFactorySourcesZipFile]",
            clTestProjectPatternAbstractFactorySourcesZipFile,
            "test/referenceCluster/ClusteringTestProject-pattern-abstractFactory.sei")
    }

}

