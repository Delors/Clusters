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

import scala.collection.mutable.Map
import framework.pipeline.Clustering
import framework.pipeline.IntermediateClustering
import framework.structure.Cluster
import framework.structure.NodeCloner
import framework.structure.util.NodeManager

/**
 * Creates hyper clusters based on greatest common prefix of classes' package names
 * @author Thomas Schlosser
 *
 */
class PackageClustering(
    val nodeManager: NodeManager,
    val successorClustering: Option[Clustering],
    val newClusterClustering: Option[Clustering])
        extends IntermediateClustering {

    protected override def process(cluster: Cluster): Cluster = {
        def getMatchingPrefix(value: String, prefixes: Array[String]): String = {
            prefixes.foreach(prfx ⇒ if (value.startsWith(prfx)) { return prfx })
            null
        }

        var prefixRoot = new GreatestCommonCharPrefixTree('#')
        for (node ← cluster.getNodes) {
            prefixRoot.addPrefix(node.identifier.toCharArray())
        }
        var prfxs = prefixRoot.prefixes.map(charArray ⇒ String.copyValueOf(charArray))

        // create resulting clusters
        val result = nodeManager.createCopy(cluster)
        var resultMap = Map[String, Cluster]()
        for (i ← 0 to prfxs.size - 1) {
            val prfx = prfxs(i)
            val cl = nodeManager.createCluster(prfx)
            resultMap(prfx) = cl
            result.addNode(cl)
        }
        for (node ← cluster.getNodes) {
            val c = resultMap(getMatchingPrefix(node.identifier, prfxs))
            c.addNode(nodeManager.createDeepCopy(node))
        }
        result
    }
}

private class GreatestCommonCharPrefixTree(
    val content: Char,
    val children: Map[Char, GreatestCommonPrefixTree[Char]])
        extends GreatestCommonPrefixTree[Char] {

    def this(content: Char) =
        this(content, Map.empty)

    override def isEndMarker(content: Char): Boolean =
        content.isUpper || content == '[' //TODO: remove if primitive (arrays) will be filtered in an earlier step 

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

object PackageClustering {

    def apply(
        nodeManager: NodeManager,
        successorClustering: Clustering = null,
        newClusterClustering: Clustering = null): PackageClustering =
        new PackageClustering(nodeManager,
            if (successorClustering == null) None else Some(successorClustering),
            if (newClusterClustering == null) None else Some(newClusterClustering))
}