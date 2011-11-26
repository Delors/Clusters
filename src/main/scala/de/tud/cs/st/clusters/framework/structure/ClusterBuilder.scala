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
package framework
package structure

import scala.collection.mutable.Map
import de.tud.cs.st.bat.resolved.dependency.DepBuilder
import de.tud.cs.st.bat.resolved.DependencyType._
import de.tud.cs.st.bat.resolved.ClassFile
import de.tud.cs.st.bat.resolved.Field_Info
import de.tud.cs.st.bat.resolved.Method_Info
import de.tud.cs.st.bat.resolved.dependencies.SourceElementIDs

/**
 * @author Thomas Schlosser
 *
 */
class ClusterBuilder extends DepBuilder with SourceElementIDs {

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
