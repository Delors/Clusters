package de.tud.cs.st.clusters.structure

class AdjacenceListEdge(val target: Int, val eType: { val id: Int; val descr: String }) {

  var predecessor: AdjacenceListEdge = _

  var successor: AdjacenceListEdge = _

  var weight: Double = 0

  def remove {
    val tempPred = this.predecessor
    val tempSucc = this.successor
    if (this.predecessor != null) {
      this.predecessor.successor = tempSucc
      this.predecessor = null
    }
    if (this.successor != null) {
      this.successor.predecessor = tempPred
      this.successor = null
    }
  }
}