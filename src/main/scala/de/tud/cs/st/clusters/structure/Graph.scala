package de.tud.cs.st.clusters.structure
import de.tud.cs.st.bat.dependency.DepGraphBuilder
import scala.collection.mutable.Map

class Graph(val name: String) extends AnyRef with DepGraphBuilder with DotableGraph {

  private var nodes = Array.empty[String]
  private var edges = Array.empty[AdjacenceListEdge]
  private var transposedEdges = Array.empty[AdjacenceListEdge]

  def this() {
    this(null)
  }

  def getID(identifier: String): Int = {
    var index = nodes.indexOf(identifier)
    if (index == -1) {
      nodes :+= identifier
      edges :+= null
      transposedEdges :+= null
      index = nodes.length - 1
    }
    index
  }

  def addEdge(src: Int, trgt: Int, eType: EdgeType) {
    def addEdge(start: Int, end: Int, adjLists: Array[AdjacenceListEdge]) {
      var successor = adjLists(start)
      val newEdge = new AdjacenceListEdge(end, eType)
      newEdge.successor = successor
      if (successor != null)
        successor.predecessor = newEdge
      adjLists(start) = newEdge
    }

    addEdge(src, trgt, edges)
    addEdge(trgt, src, transposedEdges)
  }

  def getEdges: Set[(Edge, Int)] = {
    var result = Map[Edge, Int]()
    for (i <- 0 to nodes.length - 1) {
      var e = edges(i)
      while (e != null) {
        val edge = (i, e.target, e.eType)
        result(edge) = result.getOrElse(edge, 0) + 1
        e = e.successor
      }
    }
    result.toSet
  }

  def getEdges(src: Int): AdjacenceListEdge = {
    edges(src)
  }

  def getNode(id: Int): Node =
    nodes(id)

  def size: Int =
    nodes.size
}