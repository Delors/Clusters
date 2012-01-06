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
    new pipeline.ClassClusteringTest,
    new pipeline.CombinedClusteringTest,
    new pipeline.GetterSetterClusteringTest,
    new pipeline.InternalExternalClusteringTest,
    new pipeline.LayerClusteringTest,
    new pipeline.PackageClusteringTest,
    new pipeline.StronglyConnectedComponentsClusteringTest,

    // Framework tests
    new framework.structure.util.DependencyExtractorTest)