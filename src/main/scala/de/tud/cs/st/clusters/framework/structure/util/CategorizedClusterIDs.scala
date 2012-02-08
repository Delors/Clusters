package de.tud.cs.st.clusters.framework.structure.util

/**
 * Marker trait to state that clusters are associated
 * with its own continuous sequence of ids.
 *
 * ==Implementation Note==
 * When implemented, the following invariants have to hold:
 * - LOWEST_METHOD_ID << LOWEST_CLUSTER_ID.
 * - largest(method_id) < LOWEST_CLUSTER_ID
 *
 * (LOWEST_METHOD_ID is defined in [[de.tud.cs.st.bat.resolved.CategorizedSourceElementIDs]])
 *
 * @author Thomas Schlosser
 */
trait CategorizedClusterIDs extends ClusterIDs {

    def LOWEST_CLUSTER_ID: Int

}