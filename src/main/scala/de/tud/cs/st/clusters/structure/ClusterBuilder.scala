package de.tud.cs.st.clusters.structure

import scala.collection.mutable.Map
import de.tud.cs.st.bat.resolved.dependency.DepBuilder
import de.tud.cs.st.bat.resolved.DependencyType._
import de.tud.cs.st.bat.resolved.ClassFile
import de.tud.cs.st.bat.resolved.Field_Info
import de.tud.cs.st.bat.resolved.Method_Info

class ClusterBuilder extends DepBuilder {

  private var lastUsedID = -1;

  private var idMap = Map.empty[String, Int]
  private var nodes = Array.empty[Node]

  private val cluster: Cluster = new Cluster(-1, "ROOT", true)

  def getID(identifier: String): Int =
    getID(identifier, false)

  def getID(identifier: String, clazz: ClassFile): Int =
    getID(identifier, { (i: Int, s: String) => { new ClassNode(i, s, clazz) } })

  def getID(identifier: String, field: Field_Info): Int =
    getID(identifier, { (i: Int, s: String) => { new FieldNode(i, s, field) } })

  def getID(identifier: String, method: Method_Info): Int =
    getID(identifier, { (i: Int, s: String) => { new MethodNode(i, s, method) } })

  def getID(identifier: String, isClusterNode: Boolean): Int = {
    if (isClusterNode) {
      getID(identifier, { (i: Int, s: String) => { new Cluster(i, s) } })
    } else {
      getID(identifier, { (i: Int, s: String) => { new SourceElementNode(i, s) } })
    }
  }

  def getID(identifier: String, node: (Int, String) => Node): Int = {
    idMap.getOrElseUpdate(identifier, {
      lastUsedID += 1
      nodes :+= node(lastUsedID, identifier)
      cluster.nodes :+= nodes(lastUsedID)
      lastUsedID
    })
  }

  def addDep(src: Int, trgt: Int, dType: DependencyType) {
    val source = nodes(src)
    val target = nodes(trgt)
    source.addEdge(source, nodes(trgt), dType)
    target.addEdge(source, nodes(trgt), dType)
  }

  def getNode(id: Int): Node =
    nodes(id)

  def getCluster: Cluster =
    cluster

}
