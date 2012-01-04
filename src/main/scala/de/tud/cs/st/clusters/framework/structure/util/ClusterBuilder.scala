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
package de.tud.cs.st
package clusters
package framework
package structure
package util

import scala.collection.mutable.Map
import scala.collection.mutable.ArrayBuffer
import de.tud.cs.st.bat.resolved.dependency._
import de.tud.cs.st.bat.resolved.ClassFile
import de.tud.cs.st.bat.resolved.Field
import de.tud.cs.st.bat.resolved.Method
import de.tud.cs.st.bat.resolved.Type
import de.tud.cs.st.bat.resolved.ObjectType
import de.tud.cs.st.bat.resolved.MethodDescriptor
//import de.tud.cs.st.bat.resolved.dependency.DefaultIDMappingDependencyBuilder

/**
 * @author Thomas Schlosser
 *
 */
trait ClusterBuilder extends DependencyExtractor with SourceElementIDsMap
        with ClusterIDsMap
        with PrettyPrint {

    protected val INITIAL_ARRAY_SIZE = 100000

    protected val ROOT_CLUSTER_NAME = "ROOT"

    private var typeNodes = new ArrayBuffer[TypeNode](INITIAL_ARRAY_SIZE)
    private var fieldNodes = new ArrayBuffer[FieldNode](INITIAL_ARRAY_SIZE)
    private var methodNodes = new ArrayBuffer[MethodNode](INITIAL_ARRAY_SIZE)
    private var clusterNodes = new ArrayBuffer[Cluster](INITIAL_ARRAY_SIZE)

    private var rootCluster = new Cluster(clusterID(ROOT_CLUSTER_NAME), ROOT_CLUSTER_NAME, true)

    //    abstract override def sourceElementID(clazz: ClassFile): Int = {
    //        handleIDLookup(
    //            () ⇒ super.sourceElementID(clazz),
    //            LOWEST_TYPE_ID,
    //            typeNodes,
    //            (oldNode: TypeNode) ⇒ oldNode.clazz = Some(clazz),
    //            (id) ⇒ TypeNode(id, () ⇒ prettyPrint(clazz.thisClass), clazz))
    //    }

    abstract override def sourceElementID(t: Type): Int = {
        handleIDLookup(
            () ⇒ super.sourceElementID(t),
            LOWEST_TYPE_ID,
            typeNodes,
            (_: TypeNode) ⇒ Unit,
            (id) ⇒ TypeNode(id, () ⇒ prettyPrint(t)))
    }

    //    abstract override def sourceElementID(definingObjectType: ObjectType, field: Field): Int = {
    //        handleIDLookup(
    //            () ⇒ super.sourceElementID(definingObjectType, field),
    //            LOWEST_FIELD_ID,
    //            fieldNodes,
    //            (oldNode: FieldNode) ⇒ oldNode.field = Some(field),
    //            (id) ⇒ FieldNode(id, () ⇒ prettyPrint(definingObjectType, field.name), field))
    //    }

    abstract override def sourceElementID(definingObjectType: ObjectType, fieldName: String): Int = {
        handleIDLookup(
            () ⇒ super.sourceElementID(definingObjectType, fieldName),
            LOWEST_FIELD_ID,
            fieldNodes,
            (_: FieldNode) ⇒ Unit,
            (id) ⇒ FieldNode(id, () ⇒ prettyPrint(definingObjectType, fieldName)))
    }

    //    abstract override def sourceElementID(definingObjectType: ObjectType, method: Method): Int = {
    //        handleIDLookup(
    //            () ⇒ super.sourceElementID(definingObjectType, method),
    //            LOWEST_METHOD_ID,
    //            methodNodes,
    //            (oldNode: MethodNode) ⇒ oldNode.method = Some(method),
    //            (id) ⇒ MethodNode(id, () ⇒ prettyPrint(definingObjectType, method.name, method.descriptor), method))
    //    }

    abstract override def sourceElementID(definingObjectType: ObjectType, methodName: String, methodDescriptor: MethodDescriptor): Int = {
        handleIDLookup(
            () ⇒ super.sourceElementID(definingObjectType, methodName, methodDescriptor),
            LOWEST_METHOD_ID,
            methodNodes,
            (_: MethodNode) ⇒ Unit,
            (id) ⇒ MethodNode(id, () ⇒ prettyPrint(definingObjectType, methodName, methodDescriptor)))
    }

    def getID(clusterIdentifier: String): Int = {
        handleIDLookup(
            () ⇒ clusterID(clusterIdentifier),
            LOWEST_CLUSTER_ID,
            clusterNodes,
            (_: Cluster) ⇒ Unit,
            (id) ⇒ new Cluster(id, clusterIdentifier))
    }

    def createCluster(clusterIdentifier: String): Cluster = {
        var cluster: Cluster = null
        handleIDLookup(
            //TODO check whether the uniqueness should be forced in the clusterings
            () ⇒ clusterID(clusterIdentifier + System.nanoTime()),
            LOWEST_CLUSTER_ID,
            clusterNodes,
            (_: Cluster) ⇒ sys.error("Cluster with identifier["+clusterIdentifier+"] already exists and cannot be creted again!"),
            (id) ⇒ {
                cluster = new Cluster(id, clusterIdentifier)
                cluster
            },
            false)
        cluster
    }

    private def handleIDLookup[N <: Node](
        lookupId: () ⇒ Int,
        lowestId: Int,
        nodes: ArrayBuffer[N],
        nodeExistsAction: (N) ⇒ Unit,
        newNode: (Int) ⇒ N,
        addNodeToRoot: Boolean = true): Int = {

        val id = lookupId()
        val oldNode: N = {
            if (id - lowestId >= nodes.size)
                null.asInstanceOf[N]
            else nodes(id - lowestId)
        }
        if (oldNode != null)
            nodeExistsAction(oldNode)
        else {
            val node = newNode(id)
            nodes += node
            if (addNodeToRoot)
                rootCluster.addNode(node)
        }
        id
    }

    override def processDependency(sourceID: Int, targetID: Int, dType: DependencyType) {
        getNode(sourceID).addEdge(sourceID, targetID, dType)
        getNode(targetID).addEdge(sourceID, targetID, dType)
    }

    abstract override def reset {
        super.reset
        typeNodes.clear()
        fieldNodes.clear()
        methodNodes.clear()
        clusterNodes.clear()
        rootCluster = new Cluster(clusterID(ROOT_CLUSTER_NAME), ROOT_CLUSTER_NAME, true)
    }

    def getNode(id: Int): Node = {
        if (id >= LOWEST_METHOD_ID) {
            return methodNodes(id - LOWEST_METHOD_ID)
        }
        else if (id >= LOWEST_FIELD_ID) {
            return fieldNodes(id - LOWEST_FIELD_ID)
        }
        else if (id >= LOWEST_TYPE_ID) {
            return typeNodes(id - LOWEST_TYPE_ID)
        }
        else if (id >= LOWEST_CLUSTER_ID) {
            return clusterNodes(id - LOWEST_CLUSTER_ID)
        }
        sys.error("No mapping found for ID["+id+"]")
    }

    def getRootCluster: Cluster =
        rootCluster

}
