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
import algorithm.DefaultInternalExternalClusteringStage
import algorithm.InternalExternalClusteringAlgorithmConfiguration
import algorithm.DefaultPackageClusteringStage
import algorithm.PackageClusteringAlgorithmConfiguration
import algorithm.DefaultGetterSetterClusteringStage
import algorithm.GetterSetterClusteringAlgorithmConfiguration
import algorithm.DefaultSimilarityMetricClusteringStage
import algorithm.SimilarityMetricClusteringAlgorithmConfiguration
import algorithm.DefaultStronglyConnectedComponentsClusteringStage
import algorithm.StronglyConnectedComponentsClusteringAlgorithmConfiguration
import strategy.FirstClusterablesClusteringStrategy
import strategy.IdentifierBasedClusteringStrategy
import strategy.MinClusterSizeClusteringStrategy

/**
 * @author Thomas Schlosser
 *
 */
@RunWith(classOf[JUnitRunner])
class CombinedClusteringStageTest extends AbstractClusteringTest {

    val intExtConfiguration = new InternalExternalClusteringAlgorithmConfiguration {}
    val pkgConfiguration = new PackageClusteringAlgorithmConfiguration {}
    val getterSetterConfiguration = new GetterSetterClusteringAlgorithmConfiguration {}
    val similarityMetricConfiguration = new SimilarityMetricClusteringAlgorithmConfiguration {}
    val sccConfiguration = new StronglyConnectedComponentsClusteringAlgorithmConfiguration {}

    //TODO: implement more strategies and use the metaInfo data;
    //TODO: create some concrete examples with known optimal results and evaluate the current stages and stage combinations.
    // -> e.g. pattern implementations from wikipedia
    // -> or own class that contains two different kinds of methods that are used from to different (other) classes...
    /*
     * class A{
     *   m1
     *   m2
     *   m3
     *   m4
     * }
     * 
     * class B{
     *   b1{
     *     m1
     *   }
     *   
     *   b2{
     *     m3
     *   }
     * }
     * 
     * class C{
     *   c1{
     *     m2
     *   }
     *   
     *   c2{
     *     m4
     *   }
     * }
     */

    val intExtStage = new DefaultInternalExternalClusteringStage(intExtConfiguration)
    val pkgStage = new {
        val clusterIdentifier = "external"
    } with DefaultPackageClusteringStage(pkgConfiguration) with IdentifierBasedClusteringStrategy
    val sccStage = new DefaultStronglyConnectedComponentsClusteringStage(sccConfiguration) with FirstClusterablesClusteringStrategy
    val getterSetterStage = new DefaultGetterSetterClusteringStage(getterSetterConfiguration) with FirstClusterablesClusteringStrategy
    val simMetricStage = new {
        override val minClusterSizeThreshold: Int = 3
    } with DefaultSimilarityMetricClusteringStage(similarityMetricConfiguration) with MinClusterSizeClusteringStrategy with FirstClusterablesClusteringStrategy

    implicit val clusteringStages: Array[ClusteringStage] = Array(
        intExtStage,
        pkgStage,
        sccStage,
        getterSetterStage,
        simMetricStage,
        simMetricStage,
        simMetricStage
    )

    test("testCombinedClusteringStage [ClusteringTestProject]") {
        testClustering(
            "testCombinedClusteringStage [ClusteringTestProject]",
            clusteringTestProjectDependencyExtractor,
            Some("combinedClust_ClusteringTestProject"),
            includeSingleNodes = true,
            includeEdges = true)
    }

    test("testCombinedClusteringStage [cocome]") {
        testClustering(
            "testCombinedClusteringStage [cocome]",
            cocomeDependencyExtractor,
            Some("combinedClust_cocome"),
            includeSingleNodes = true,
            includeEdges = true)
    }

    test("testCombinedClusteringStage [hibernate]") {
        testClustering(
            "testCombinedClusteringStage [hibernate]",
            hibernateDependencyExtractor,
            Some("combinedClust_hibernate"))
    }

}
