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
package pipeline

import structure.Node
import structure.Cluster
import structure.SourceElementNode

/**
 * @author Thomas Schlosser
 *
 */
class GMLClusteringResultWriter(
    val gmlFileName: String)
        extends ClusteringResultWriter(gmlFileName, "gml") {

    override protected def writeHeader() {
        write("Creator\t\"Clusters\"\nVersion\t\"0.1\"\ngraph\n[\n\tdirected\t1\n")
    }

    override protected def writeFooter() {
        write("]")
    }

    override protected def writeCluster(cluster: Cluster, nodeBuffer: StringBuffer, edgeBuffer: StringBuffer) {
        // Example:
        //      node
        //	[
        //		id	4
        //		label	"Group 1"
        //		isGroup	1
        //	]
        nodeBuffer.append("\tnode\n")
        nodeBuffer.append("\t[\n")
        nodeBuffer.append("\t\tid\t\t")
        nodeBuffer.append(cluster.uniqueID)
        nodeBuffer.append("\n\t\tlabel\t\"")
        nodeBuffer.append(cluster.identifier)
        nodeBuffer.append("\"\n")
        if (cluster.parent != null) {
            nodeBuffer.append("\t\tgid\t\t")
            nodeBuffer.append(cluster.parent.uniqueID)
            nodeBuffer.append("\n")
        }
        nodeBuffer.append("\t\tisGroup\t1\n\t]\n")

        //        nodeBuffer.append("\t\tisGroup\t1\n\t\tLabelGraphics\n\t\t[\n\t\t\ttext\t\"")
        //        nodeBuffer.append(cluster.identifier)
        //        nodeBuffer.append("\"\n\t\t]\n\t]")

        cluster.getNodes foreach {
            case c: Cluster ⇒
                writeCluster(c, nodeBuffer, edgeBuffer)
            case sen: SourceElementNode ⇒
                writeSourceElementNode(sen, nodeBuffer, edgeBuffer)
        }

        writeEdges(cluster, edgeBuffer)
    }

    override protected def writeSourceElementNode(node: SourceElementNode, nodeBuffer: StringBuffer, edgeBuffer: StringBuffer) {
        // Example:
        //	node
        //	[
        //		id	0
        //		label	"A"
        //		gid     4
        //	]
        nodeBuffer.append("\tnode\n")
        nodeBuffer.append("\t[\n")
        nodeBuffer.append("\t\tid\t\t")
        nodeBuffer.append(node.uniqueID)
        nodeBuffer.append("\n\t\tlabel\t\"")
        nodeBuffer.append(node.identifier)
        nodeBuffer.append("\"\n")
        if (node.parent != null) {
            nodeBuffer.append("\t\tgid\t\t")
            nodeBuffer.append(node.parent.uniqueID)
            nodeBuffer.append("\n")
        }
        nodeBuffer.append("\t]\n")

        writeEdges(node, edgeBuffer)
    }

    private def writeEdges(node: Node, edgeBuffer: StringBuffer) {
        // Example:
        //      edge
        //	[
        //		source	0
        //		target	1
        //		label   "AB"
        //	]
        // add egdes
        for (e ← node.getOwnEdges) {
            edgeBuffer.append("\tedge\n")
            edgeBuffer.append("\t[\n")
            edgeBuffer.append("\t\tsource\t")
            edgeBuffer.append(e.source.uniqueID)
            edgeBuffer.append("\n\t\ttarget\t")
            edgeBuffer.append(e.target.uniqueID)
            edgeBuffer.append("\n\t\tlabel\t\"")
            edgeBuffer.append(e.dType.toString)
            edgeBuffer.append(" [")
            edgeBuffer.append(e.count)
            edgeBuffer.append("]\"\n\t]\n")
        }
    }
}
