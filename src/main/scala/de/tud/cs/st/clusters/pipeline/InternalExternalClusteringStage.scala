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

import scala.collection.mutable.Set
import framework.pipeline.ClusteringStage
import framework.pipeline.ClusteringStageConfiguration
import framework.structure.Cluster
import framework.structure.TypeNode
import framework.structure.FieldNode
import framework.structure.MethodNode
import framework.structure.util.ClusterManager

/**
 * Splits the nodes into internal and external cluster.
 * A node is added to the internal cluster if a type with a package that prefixes the package
 * of that node exists. A package prefixes another package only if every sub-package matches as a whole.
 * All other nodes are added to the external cluster.
 *
 * The <code>newClusterClustering</code> parameter is not needed/used in this class, because
 * a more specific configuration can be done by using <code>internalClustering</code> and
 * <code>externalClustering</code>.
 *
 * @author Thomas Schlosser
 *
 */
trait InternalExternalClusteringStage extends ClusteringStage[InternalExternalClusteringStageConfiguration] {

    protected override def process(cluster: Cluster): Cluster = {
        // create list that contains all names of internal packages
        var internalPackages: Set[String] = Set()
        for (node ← cluster.getNodes) {
            node match {
                case TypeNode(_, _, Some(t)) ⇒
                    internalPackages = internalPackages + (t.thisClass.packageName.replace('/', '.') + '.')
                case _ ⇒
                // nothing to do in this case, because the node is not associated with a classFile object
                // fields and methods can be omitted, since their packages will be added to the list
                // when their declaring class is analyzed 
            }
        }

        // reduce set to unique prefixes
        def removeLongerPackagePrefix(pkg: String) {
            for (pkg2 ← internalPackages if (pkg != pkg2 && pkg.startsWith(pkg2))) {
                internalPackages = internalPackages - pkg
                return
            }
        }

        internalPackages foreach { removeLongerPackagePrefix(_) }

        val inputNodes = cluster.getNodes.toArray
        cluster.clearNodes()
        cluster.clusterable = false
        val internal = clusterManager.createCluster("internal")
        val external = clusterManager.createCluster("external")
        //TODO: remove
        external.clusterable = false
        cluster.addNode(internal)
        cluster.addNode(external)
        for (node ← inputNodes) {
            if (internalPackages exists (node.identifier.startsWith(_))) {
                internal.addNode(node)
            }
            else {
                external.addNode(node)
            }
        }

        cluster
    }
}

trait InternalExternalClusteringStageConfiguration extends ClusteringStageConfiguration {

}

object InternalExternalClusteringStage {

    def apply(c: InternalExternalClusteringStageConfiguration): InternalExternalClusteringStage =
        new { override val configuration = c } with InternalExternalClusteringStage

}
