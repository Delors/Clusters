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

import scala.collection.mutable.Map
import framework.pipeline.ClusteringAlgorithm
import framework.pipeline.ClusteringAlgorithmConfiguration
import framework.structure.Cluster
import framework.structure.util.ClusterManager

/**
 * Creates hyper clusters based on greatest common prefix of classes' package names
 * @author Thomas Schlosser
 *
 */
trait PackageClusteringStage extends ClusteringAlgorithm[PackageClusteringAlgorithmConfiguration] {

    override def performClustering(cluster: Cluster): Cluster = {
        def getMatchingPrefix(value: String, prefixes: Array[String]): String = {
            prefixes.foreach(prfx ⇒ if (value.startsWith(prfx)) { return prfx })
            sys.error("No matching prefix found for \""+value+"\" in prefixes: "+prefixes.mkString("\n"))
        }

        var prefixRoot = new GreatestCommonCharPrefixTree('#')
        for (node ← cluster.getNodes) {
            prefixRoot.addPrefix(node.identifier.toCharArray())
        }
        var prfxs = prefixRoot.prefixes.map(charArray ⇒ String.copyValueOf(charArray))

        val inputNodes = cluster.getNodes.toArray
        cluster.clearNodes()

        // create resulting clusters
        var resultMap = Map[String, Cluster]()
        for (i ← 0 to prfxs.size - 1) {
            val prfx = prfxs(i)
            val cl = clusterManager.createCluster(prfx, this.stageName)
            cl.clusterable = !algorithmConfig.createUnclusterableClusters
            resultMap(prfx) = cl
            cluster.addNode(cl)
        }
        for (node ← inputNodes) {
            val c = resultMap(getMatchingPrefix(node.identifier, prfxs))
            c.addNode(node)
        }
        cluster
    }
}

private class GreatestCommonCharPrefixTree(
    val content: Char,
    val children: Map[Char, GreatestCommonPrefixTree[Char]])
        extends GreatestCommonPrefixTree[Char] {

    def this(content: Char) =
        this(content, Map.empty)

    override def isEndMarker(content: Char): Boolean =
        content.isUpper

    override def GreatestCommonPrefixTree(content: Char): GreatestCommonCharPrefixTree =
        new GreatestCommonCharPrefixTree(content)
}

private trait GreatestCommonPrefixTree[Content] {
    val content: Content
    val children: Map[Content, GreatestCommonPrefixTree[Content]]
    var end: Boolean = false

    def addPrefix(prefix: Array[Content]) {
        var predecessor: GreatestCommonPrefixTree[Content] = null
        var current: GreatestCommonPrefixTree[Content] = this
        prefix.foreach(c ⇒ {
            if (isEndMarker(c)) {
                current.end = true
                current.children.clear()
                return
            }
            current.children.get(c) match {
                case Some(c2) ⇒
                    predecessor = current
                    current = c2
                case None ⇒
                    if (current.end) {
                        return
                    }
                    current.children(c) = GreatestCommonPrefixTree(c)

                    predecessor = current
                    current = current.children(c)
            }
        })
    }

    def prefixes(implicit m: ClassManifest[Content]): Array[Array[Content]] = {
        var result: Array[Array[Content]] = Array()
        for ((key, value) ← children) {
            var subResult: Array[Array[Content]] = value.prefixes.map(prfx ⇒ Array(key) ++ prfx)
            if (subResult.length == 0) {
                subResult = Array(Array(key))
            }
            result ++= subResult
        }
        result
    }

    def isEndMarker(content: Content): Boolean

    def GreatestCommonPrefixTree(content: Content): GreatestCommonPrefixTree[Content]

}

trait PackageClusteringAlgorithmConfiguration extends ClusteringAlgorithmConfiguration {
    // new clusters should be marked as unclusterable if they are only libraries that should not further be considered
    val createUnclusterableClusters = true
}

class DefaultPackageClusteringStage(
    val algorithmConfig: PackageClusteringAlgorithmConfiguration)
        extends PackageClusteringStage {

}
