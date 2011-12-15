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

    // Cluster Filter tests...
    new filter.CombinedClusteringTest,
    new filter.GetterSetterClusteringTest,
    new filter.HyperClusterFilterTest,
    new filter.InternExternClusterFilterTest,
    new filter.LayerClusteringTest,
    new filter.StronglyConnectedComponentsClusteringTest,

    // Framework tests
    new framework.structure.util.DependencyExtractorTest)