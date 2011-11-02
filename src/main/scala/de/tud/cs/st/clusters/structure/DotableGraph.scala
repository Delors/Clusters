package de.tud.cs.st.clusters.structure
import de.tud.cs.st.bat.dependency.EdgeType

trait DotableGraph {

  type Edge = (Int, Int, EdgeType)
  type Node = String

  def getEdges: Set[Edge]

  def getNode(id: Int): Node

  def toDot(): String = {
    var processedNodes = Set.empty[Int]

    var s = "digraph G {\n"

    for ((src, trgt, eType) <- getEdges) {
      if (!processedNodes.contains(src)) {
        s += "\t" + src + "[label=\"" + getNode(src) + "\"];\n"
        processedNodes += src
      }
      if (!processedNodes.contains(trgt)) {
        s += "\t" + trgt + "[label=\"" + getNode(trgt) + "\"];\n"
        processedNodes += trgt
      }

      s += "\t" + src + " -> " + trgt + "[label=\"" + eType.descr + "\"];\n" // +"[dir=none];\n"
    }

    s += "}"
    s
  }
}