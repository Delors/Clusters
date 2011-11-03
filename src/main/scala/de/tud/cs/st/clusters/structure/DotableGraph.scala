package de.tud.cs.st.clusters.structure

trait DotableGraph {

  type EdgeType = { val id: Int; val descr: String }
  type Edge = (Int, Int, EdgeType)
  type Node = String

  def getEdges: Set[(Edge, Int)]

  def getNode(id: Int): Node

  def size: Int

  def name: String

  def toDot(): String = {
    var s = "digraph \"" + { if (name != null) name else "G" } + "\" {\n"

    // add nodes
    for (i <- 0 to size - 1) {
      s += "\t" + i + "[label=\"" + getNode(i) + "\"];\n"
    }

    // add egdes
    for (((src, trgt, eType), cnt) <- getEdges) {
      s += "\t" + src + " -> " + trgt + "[label=\"" + eType.descr + "[" + cnt + "]\"];\n"
    }

    s += "}"
    s
  }
}