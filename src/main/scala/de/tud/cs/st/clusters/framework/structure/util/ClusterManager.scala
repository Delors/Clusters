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
package util

import de.tud.cs.st.bat.resolved.dependency._
import de.tud.cs.st.bat.resolved.Type
import de.tud.cs.st.bat.resolved.ObjectType
import de.tud.cs.st.bat.resolved.ReferenceType
import de.tud.cs.st.bat.resolved.MethodDescriptor
import de.tud.cs.st.bat.resolved.SourceElementIDs
import de.tud.cs.st.bat.resolved.SourceElementIDsMap
import de.tud.cs.st.bat.resolved.UseIDOfBaseTypeForArrayTypes

/**
 * @author Thomas Schlosser
 *
 */
trait ClusterManager
        extends SourceElementIDs
        with ClusterIDs
        with NodeFactory
        with ClusterFactory
        with NodeStore {

    protected val PROJECT_CLUSTER_NAME = "project"

    private val projectCluster = new Cluster(clusterID(PROJECT_CLUSTER_NAME), PROJECT_CLUSTER_NAME)

    abstract override def sourceElementID(t: Type): Int = {
        val id = super.sourceElementID(t)
        val node = createTypeNode(id, t)
        projectCluster.addChild(node)
        id
    }

    abstract override def sourceElementID(definingObjectType: ObjectType, fieldName: String): Int = {
        val id = super.sourceElementID(definingObjectType, fieldName)
        val node = createFieldNode(id, definingObjectType, fieldName)
        projectCluster.addChild(node)
        id
    }

    abstract override def sourceElementID(definingReferenceType: ReferenceType, methodName: String, methodDescriptor: MethodDescriptor): Int = {
        val id = super.sourceElementID(definingReferenceType, methodName, methodDescriptor)
        val node = createMethodNode(id, definingReferenceType, methodName, methodDescriptor)
        projectCluster.addChild(node)
        id
    }

    def processDependency(sourceID: Int, targetID: Int, dType: DependencyType) {
        getNode(sourceID).addEdge(getNode(targetID), dType)
    }

    def getProjectCluster: Cluster =
        projectCluster

}

class DefaultClusterManager
    extends SourceElementIDsMap
    with ClusterIDsMap
    with ClusterManager
    with UseIDOfBaseTypeForArrayTypes
