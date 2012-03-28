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
package algorithm

import scala.collection.mutable.Set
import framework.pipeline.ClusteringAlgorithm
import framework.structure.Cluster
import framework.structure.TypeNode

/**
 * Splits the nodes into application and libraries cluster.
 * A node is added to the application cluster if a type with a package exists, that prefixes the package
 * of that node. A package prefixes another package only if every sub-package matches as a whole.
 * All other nodes are added to the libraries cluster.
 *
 * @author Thomas Schlosser
 *
 */
class ApplicationLibrariesSeparator(
    val config: ApplicationLibrariesSeparatorConfiguration)
        extends ClusteringAlgorithm {

    /**
     * Character that marks the end of a package string
     */
    private val EOP: Char = '/'

    protected def doPerformClustering(cluster: Cluster): Boolean = {
        // create list that contains all names of the application packages
        var applicationPackages: Set[String] = Set()
        for (child ← cluster.children) {
            child match {
                case TypeNode(_, _, Some(t)) ⇒
                    // At the end, a '/' is added, because sub-packages on the last
                    // package level should not match with other sub-packages
                    // that have this package as prefix.
                    applicationPackages = applicationPackages + (t.thisClass.packageName + EOP)
                case _ ⇒
                // nothing to do in this case, because the node is not associated with a classFile object
                // fields and methods can be omitted, since their packages will be added to the list
                // when their declaring class is analyzed
            }
        }

        // reduce set to unique prefixes
        def removeLongerPackagePrefix(pkg: String) {
            for (pkg2 ← applicationPackages if (pkg != pkg2 && pkg.startsWith(pkg2))) {
                applicationPackages = applicationPackages - pkg
                return
            }
        }
        // TODO do consider using the "map" function ("merge" in the above method)
        applicationPackages foreach { removeLongerPackagePrefix(_) }

        val inputChildren = cluster.children.toArray
        cluster.clearChildren()
        cluster.clusterable = false
        val applicationCluster = clusterManager.createCluster(config.applicationClusterIdentifier, this.stageName)
        val librariesCluster = clusterManager.createCluster(config.librariesClusterIdentifier, this.stageName)
        librariesCluster.clusterable = !config.markLibrariesAsUnclusterable
        cluster.addChild(applicationCluster)
        cluster.addChild(librariesCluster)
        for (child ← inputChildren) {
            if (applicationPackages.exists(appPkg ⇒ child.identifier.declaringPackage.exists(p ⇒ (p + EOP).startsWith(appPkg)))) {
                applicationCluster.addChild(child)
            }
            else {
                librariesCluster.addChild(child)
            }
        }

        // always return TRUE, because application and libraries clusters
        // are created regardless of whether the clusters are empty or not.
        true
    }
}

trait ApplicationLibrariesSeparatorConfiguration {
    val applicationClusterIdentifier = "application"
    val librariesClusterIdentifier = "libraries"

    val markLibrariesAsUnclusterable = true
}

object ApplicationLibrariesSeparatorConfiguration extends ApplicationLibrariesSeparatorConfiguration
