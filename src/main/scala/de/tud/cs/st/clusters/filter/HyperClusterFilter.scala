package de.tud.cs.st.clusters.filter
import java.io.File
import scala.collection.mutable.Map
import de.tud.cs.st.bat.dependency.CustomType

/**
 * Create hyper clusters based on greatest common prefix of classes' package names
 */
trait HyperClusterFilter extends ClusterFilter {
  type Graph = de.tud.cs.st.clusters.structure.Graph
  type Dir = File

  abstract override def filter(clusters: Array[Graph], projectRootDir: Dir): Array[Graph] = {
    var newClusters = Array.empty[Graph]
    for (cluster <- clusters) {
      newClusters ++= createHyperClusters(cluster, projectRootDir)
    }
    super.filter(newClusters, projectRootDir)
  }

  private def createHyperClusters(cluster: Graph, projectRootDir: Dir): Array[Graph] = {
    def getMatchingPrefix(value: String, prefixes: Array[String]): String = {
      prefixes.foreach(prfx => if (value.startsWith(prfx)) { return prfx })
      null
    }

    var prefixRoot = new GreatestCommonCharPrefixTree('#')
    for (i <- 0 to cluster.size - 1) {
      prefixRoot.addPrefix(cluster.getNode(i).toCharArray())
    }
    var prfxs = prefixRoot.prefixes.map(charArray => String.copyValueOf(charArray))

    // create resulting clusters
    var resultMap = Map[String, Graph]().empty
    for (i <- 0 to prfxs.size - 1) {
      val prfx = prfxs(i)
      resultMap(prfx) = new Graph(prfx)
    }
    for (i <- 0 to cluster.size - 1) {
      val node = cluster.getNode(i)
      val g = resultMap(getMatchingPrefix(node, prfxs))
      val id = g.getID(node)
      val clusterName = g.name
      //TODO: add all edges...how should edges to nodes of other clusters be handled?
      //TODO: transposed edges?!?
      var e = cluster.getEdges(i)
      while (e != null) {
        val trgtName = cluster.getNode(e.target)
        if (trgtName.startsWith(clusterName)) {
          g.addEdge(id, g.getID(trgtName), e.eType)
        } else {
          // edges to nodes of other clusters are connected to a new node that represents this cluster
          val trgtClusterName = getMatchingPrefix(trgtName, prfxs)
          g.addEdge(id, g.getID(trgtClusterName), e.eType)
          val trgtClusterGraph = resultMap(trgtClusterName)
          trgtClusterGraph.addEdge(trgtClusterGraph.getID(clusterName), trgtClusterGraph.getID(trgtName), e.eType)
        }
        e = e.successor
      }

    }
    resultMap.values.toArray
  }
}

private class GreatestCommonCharPrefixTree(val content: Char, val children: Map[Char, GreatestCommonPrefixTree[Char]]) extends GreatestCommonPrefixTree[Char] {

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
    prefix.foreach(c => {
      if (isEndMarker(c)) {
        current.end = true
        current.children.clear()
        return
      }
      current.children.get(c) match {
        case Some(c2) =>
          predecessor = current
          current = c2
        case None =>
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
    for ((key, value) <- children) {
      var subResult: Array[Array[Content]] = value.prefixes.map(prfx => Array(key) ++ prfx)
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