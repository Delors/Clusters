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
import de.tud.cs.st.clusters.pipeline.algorithm.BasePackageExtractor
import de.tud.cs.st.clusters.pipeline.algorithm.BasePackageExtractorConfiguration
import de.tud.cs.st.clusters.pipeline.algorithm.ImplementationTestingSeparator
import de.tud.cs.st.clusters.pipeline.algorithm.ImplementationTestingSeparatorConfiguration
import de.tud.cs.st.clusters.pipeline.algorithm.GetterSetterClustering
import de.tud.cs.st.clusters.pipeline.algorithm.GetterSetterClusteringConfiguration
import de.tud.cs.st.clusters.pipeline.algorithm.LayerClustering
import de.tud.cs.st.clusters.pipeline.algorithm.LayerClusteringConfiguration
import de.tud.cs.st.clusters.pipeline.algorithm.ChineseWhispers
import de.tud.cs.st.clusters.pipeline.algorithm.ChineseWhispersConfiguration
import de.tud.cs.st.clusters.pipeline.algorithm.StronglyConnectedComponentsClustering
import de.tud.cs.st.clusters.pipeline.algorithm.StronglyConnectedComponentsClusteringConfiguration
import de.tud.cs.st.clusters.pipeline.strategy.FirstClusterablesClusteringStrategy
import de.tud.cs.st.clusters.pipeline.strategy.FixedPointIterationClusteringStrategy
import de.tud.cs.st.clusters.pipeline.strategy.IdentifierBasedClusteringStrategy
import de.tud.cs.st.clusters.pipeline.strategy.MinClusterSizeClusteringStrategy
import util.ReferenceClusterCreator
import util.SourceFile
import framework.structure.Cluster
import framework.structure.util.ClusterManager
import framework.structure.SourceElementNode
import de.tud.cs.st.bat.resolved.dependency._
import de.tud.cs.st.util.perf._

/**
 * @author Thomas Schlosser
 *
 */
@RunWith(classOf[JUnitRunner])
abstract class AbstractEvaluationTest extends AbstractClusteringTest {

    val appLibsConfig = new ApplicationLibrariesSeparatorConfiguration {}
    val basePkgConfig = new BasePackageExtractorConfiguration {}
    val basePkgConfigCreateClusterable = new BasePackageExtractorConfiguration {
        override val createUnclusterableClusters = false
    }
    val implTestConfig = new ImplementationTestingSeparatorConfiguration {}
    val getterSetterConfig = new GetterSetterClusteringConfiguration {}
    val chineseWhispersConfig = new ChineseWhispersConfiguration {}
    val chineseWhispersEquallyWeightedConfig = new ChineseWhispersConfiguration {
        override def getWeight(dType: DependencyType): Long = 1
    }
    val sccConfig = new StronglyConnectedComponentsClusteringConfiguration {}
    val layerConfig = new LayerClusteringConfiguration {}

    val appLibsSeparator = new ApplicationLibrariesSeparator(appLibsConfig)
    val libsBasePackageExtractor = new {
        val clusterIdentifier = appLibsConfig.librariesClusterIdentifier
    } with BasePackageExtractor(basePkgConfig) with IdentifierBasedClusteringStrategy
    val implBasePackageExtractor = new {
        val clusterIdentifier = implTestConfig.implementationClusterIdentifier
    } with BasePackageExtractor(basePkgConfigCreateClusterable) with IdentifierBasedClusteringStrategy
    val implTestSeparator = new ImplementationTestingSeparator(implTestConfig) with FirstClusterablesClusteringStrategy
    val sccClustering = new StronglyConnectedComponentsClustering(sccConfig) with FirstClusterablesClusteringStrategy
    val getterSetterClustering = new GetterSetterClustering(getterSetterConfig) with FirstClusterablesClusteringStrategy
    val chineseWhispers = new {
        override val minClusterSizeThreshold: Int = 3
    } with ChineseWhispers(chineseWhispersConfig) with MinClusterSizeClusteringStrategy with FirstClusterablesClusteringStrategy with FixedPointIterationClusteringStrategy

    val chineseWhispersEquallyWeighted = new {
        override val minClusterSizeThreshold: Int = 3
    } with ChineseWhispers(chineseWhispersEquallyWeightedConfig) with MinClusterSizeClusteringStrategy with FirstClusterablesClusteringStrategy with FixedPointIterationClusteringStrategy

    val layerClustering = new {
        val clusterIdentifier = implTestConfig.implementationClusterIdentifier
    } with LayerClustering(layerConfig) with FirstClusterablesClusteringStrategy with IdentifierBasedClusteringStrategy
    val classExtractor = new ClassExtractor() with FirstClusterablesClusteringStrategy

    val onlyChineseWhispers: Array[ClusteringStage] = Array(chineseWhispers)
    val combinedStagesChineseWhispers: Array[ClusteringStage] = Array(
        appLibsSeparator,
        libsBasePackageExtractor,
        implTestSeparator,
        sccClustering,
        getterSetterClustering,
        chineseWhispers
    )
    val onlyChineseWhispersEquallyWeighted: Array[ClusteringStage] = Array(chineseWhispersEquallyWeighted)
    val combinedStagesChineseWhispersEquallyWeighted: Array[ClusteringStage] = Array(
        appLibsSeparator,
        libsBasePackageExtractor,
        implTestSeparator,
        sccClustering,
        getterSetterClustering,
        chineseWhispersEquallyWeighted
    )
    val onlyLayerClustering: Array[ClusteringStage] = Array(new LayerClustering(layerConfig))
    val combinedStagesLayerClustering: Array[ClusteringStage] = Array(
        appLibsSeparator,
        libsBasePackageExtractor,
        implTestSeparator,
        sccClustering,
        getterSetterClustering,
        layerClustering
    )
    val onlyClassExtractor: Array[ClusteringStage] = Array(classExtractor)
    val combinedStagesClassExtractor: Array[ClusteringStage] = Array(
        appLibsSeparator,
        libsBasePackageExtractor,
        implTestSeparator,
        sccClustering,
        getterSetterClustering,
        classExtractor
    )
    val combinedStagesClassExChineseWhisp: Array[ClusteringStage] = Array(
        appLibsSeparator,
        libsBasePackageExtractor,
        implTestSeparator,
        sccClustering,
        getterSetterClustering,
        classExtractor,
        chineseWhispers
    )
    val combinedStagesClassExChineseWhispEqWe: Array[ClusteringStage] = Array(
        appLibsSeparator,
        libsBasePackageExtractor,
        implTestSeparator,
        sccClustering,
        getterSetterClustering,
        classExtractor,
        chineseWhispersEquallyWeighted
    )
    val combinedStagesWithoutFinalizer: Array[ClusteringStage] = Array(
        appLibsSeparator,
        libsBasePackageExtractor,
        implTestSeparator,
        sccClustering,
        getterSetterClustering
    )
    val onlyAppLibsSeparator: Array[ClusteringStage] = Array(appLibsSeparator)
    val onlyGetterSetterClustering: Array[ClusteringStage] = Array(getterSetterClustering)
    val onlyImplTestSeparator: Array[ClusteringStage] = Array(implTestSeparator)
    val onlyBasePackageExtractor: Array[ClusteringStage] = Array(new BasePackageExtractor(basePkgConfig))
    val onlySCCClustering: Array[ClusteringStage] = Array(sccClustering)

    val packageClassBasedClustering: Array[ClusteringStage] = Array(
        appLibsSeparator,
        libsBasePackageExtractor,
        implTestSeparator,
        implBasePackageExtractor,
        classExtractor
    )

    val allStageCombos: Array[(String, Array[ClusteringStage])] = Array(
        ("onlyChineseWhispers", onlyChineseWhispers),
        ("combinedStagesChineseWhispers", combinedStagesChineseWhispers),
        ("onlyChineseWhispersEquallyWeighted", onlyChineseWhispersEquallyWeighted),
        ("combinedStagesChineseWhispersEquallyWeighted", combinedStagesChineseWhispersEquallyWeighted),
        ("onlyLayerClustering", onlyLayerClustering),
        ("combinedStagesLayerClustering", combinedStagesLayerClustering),
        ("onlyClassExtractor", onlyClassExtractor),
        ("combinedStagesClassExtractor", combinedStagesClassExtractor),
        ("combinedStagesClassExChineseWhisp", combinedStagesClassExChineseWhisp),
        ("combinedStagesClassExChineseWhispEqWe", combinedStagesClassExChineseWhispEqWe),
        ("combinedStagesWithoutFinalizer", combinedStagesWithoutFinalizer),
        ("onlyAppLibsSeparator", onlyAppLibsSeparator),
        ("onlyGetterSetterClustering", onlyGetterSetterClustering),
        ("onlyImplTestSeparator", onlyImplTestSeparator),
        ("onlyBasePackageExtractor", onlyBasePackageExtractor),
        ("onlySCCClustering", onlySCCClustering),
        ("packageClassBasedClustering", packageClassBasedClustering),
        ("noClustering", Array())
    )

    private def evaluate(
        projectName: String,
        sourceFiles: SourceFile,
        referenceClusteringFilePath: String,
        testRuns: Int = 100,
        measuredRuns: Int = 50) {
        evaluate(projectName, sourceFiles, referenceClusteringFilePath, testRuns, measuredRuns, allStageCombos)
    }

    protected def evaluate(
        projectName: String,
        sourceFiles: SourceFile,
        referenceClusteringFilePath: String,
        testRuns: Int,
        measuredRuns: Int,
        allStageCombos: Array[(String, Array[ClusteringStage])])

    test("evaluate [Flashcards]") {
        evaluate(
            "Flashcards",
            flashcardsSourceZipFile,
            "test/referenceCluster/Flashcards 0.4 - target 1.6.sei")
    }

    test("evaluate [CoCoME]") {
        evaluate(
            "CoCoME",
            cocomeSourceZipFile,
            "test/referenceCluster/cocome-impl-classes.sei")
    }

    // TODO: there are no valid reference clusters...
    test("evaluate [hibernate]") {
        evaluate(
            "Hibernate",
            hibernateSourceZipFile,
            "test/referenceCluster/hibernate-core-3.6.0.Final.sei")
    }

    test("evaluate [ClusteringTestProject]") {
        evaluate(
            "ClusteringTestProject",
            clusteringTestProjectSourceZipFile,
            "test/referenceCluster/ClusteringTestProject.sei")
    }

    test("evaluate [clTestProjectExampleCrosscuttingConcernSourcesZipFile]") {
        evaluate(
            "CrosscuttingConcern",
            clTestProjectExampleCrosscuttingConcernSourcesZipFile,
            "test/referenceCluster/ClusteringTestProject-example-crosscuttingConcern.sei")
    }

    test("evaluate [clTestProjectExampleMixedConcernSourcesZipFile]") {
        evaluate(
            "MixedConcern",
            clTestProjectExampleMixedConcernSourcesZipFile,
            "test/referenceCluster/ClusteringTestProject-example-mixedConcern.sei")
    }

    test("evaluate [clTestProjectPatternAbstractFactorySourcesZipFile]") {
        evaluate(
            "AbstractFactoryPattern",
            clTestProjectPatternAbstractFactorySourcesZipFile,
            "test/referenceCluster/ClusteringTestProject-pattern-abstractFactory.sei")
    }

    test("evaluate [antSourceZipFile]") {
        evaluate(
            "ANT",
            antSourceZipFile,
            null)
    }

    test("evaluate [clustersSourceZipFile]") {
        evaluate(
            "Clusters",
            clustersSourceZipFile,
            null)
    }

    test("evaluate [javaRuntimeSourceZipFile]") {
        evaluate(
            "javaRuntime",
            javaRuntimeSourceZipFile,
            null,
            testRuns = 1,
            measuredRuns = 1)
    }

    test("evaluate [ant183SourceZipFile]") {
        evaluate(
            "ANT-1.8.3",
            ant183SourceZipFile,
            null)
    }

    test("evaluate [argoUMLSourceZipFile]") {
        evaluate(
            "ArgoUML",
            argoUMLSourceZipFile,
            null)
    }

    test("evaluate [derbySourceZipFile]") {
        evaluate(
            "derby",
            derbySourceZipFile,
            null)
    }

    test("evaluate [droolsSourceZipFile]") {
        evaluate(
            "drools",
            droolsSourceZipFile,
            null)
    }

    test("evaluate [eclipseJdtUISourceZipFile]") {
        evaluate(
            "eclipseJdtUI",
            eclipseJdtUISourceZipFile,
            null)
    }

    test("evaluate [saxonSourceZipFile]") {
        evaluate(
            "SAXON",
            saxonSourceZipFile,
            null)
    }

    test("evaluate [springFrameworkSourceZipFile]") {
        evaluate(
            "SpringFramework",
            springFrameworkSourceZipFile,
            null)
    }
}

