package de.tud.cs.st.clusters

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Suites

/**
 * Runs all tests related to Clusters (except runtime tests).
 *
 * @author Thomas Schlosser
 */
@RunWith(classOf[JUnitRunner])
class ClustersSuite extends Suites(

    // Clustering tests...
    new pipeline.ApplicationLibrariesSeparatorStageTest,
    new pipeline.ClassExtractorStageTest,
    new pipeline.CombinedClusteringStageTest,
    new pipeline.GetterSetterClusteringStageTest,
    new pipeline.ImplementationTestingSeparatorStageTest,
    new pipeline.LayerClusteringStageTest,
    new pipeline.PackageClusteringStageTest,
    new pipeline.SimilarityMetricClusteringStageTest,
    new pipeline.StronglyConnectedComponentsClusteringStageTest,

    // Framework tests
    new framework.structure.util.DependencyExtractorTest)