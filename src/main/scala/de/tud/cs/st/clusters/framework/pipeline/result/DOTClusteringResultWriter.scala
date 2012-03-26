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
package result

import structure.Cluster
import structure.SourceElementNode
import structure.Node

/**
 * @author Thomas Schlosser
 *
 */
class DOTClusteringResultWriter(
    val dotFileName: String,
    val configuration: DOTClusteringResultWriterConfiguration)
        extends ClusteringResultFileWriter(dotFileName, "dot") {

    override protected def writeHeader() {
        write("digraph G {\n")
    }

    override protected def writeFooter() {
        write("}\n")
    }

    override protected def writeCluster(cluster: Cluster, nodeBuffer: StringBuffer, edgeBuffer: StringBuffer) {
        val subGraphBuffer = new StringBuffer()
        if (!cluster.isProjectCluster) {
            subGraphBuffer.append("subgraph cluster_")
            subGraphBuffer.append(cluster.uniqueID)
            subGraphBuffer.append(" {\n")
        }

        var emptyCluster = true
        // add nodes
        cluster.children foreach {
            case c: Cluster ⇒
                if (emptyCluster) {
                    nodeBuffer.append(subGraphBuffer)
                    emptyCluster = false
                }
                writeCluster(c, nodeBuffer, edgeBuffer)
            case n: SourceElementNode ⇒
                if (configuration.includeSingleNodes && emptyCluster) {
                    nodeBuffer.append(subGraphBuffer)
                    emptyCluster = false
                }
                writeSourceElementNode(n, nodeBuffer, edgeBuffer)
        }

        // add egdes
        writeEdges(cluster, edgeBuffer)

        if (emptyCluster) {
            nodeBuffer.append("\t\"")
            nodeBuffer.append(cluster.uniqueID)
            nodeBuffer.append("\"[shape=box, label=\""+cluster.identifier+"\"];\n")
        }
        else if (!cluster.isProjectCluster) {
            nodeBuffer.append("\tnode [style=filled,fillcolor=white,color=black];\n")
            nodeBuffer.append("\tstyle=filled;\n")
            nodeBuffer.append("\tfillcolor=lightgrey;\n")
            nodeBuffer.append("\tcolor=black;\n")
            nodeBuffer.append("\tlabel = \"")
            nodeBuffer.append(cluster.identifier)
            nodeBuffer.append("\";\n")
            nodeBuffer.append("}\n")
        }
    }

    private def writeSourceElementNode(node: SourceElementNode, nodeBuffer: StringBuffer, edgeBuffer: StringBuffer) {
        if (configuration.includeSingleNodes) {
            nodeBuffer.append("\t")
            nodeBuffer.append(node.uniqueID)
            nodeBuffer.append("[label=\"")
            nodeBuffer.append(node.identifier)
            nodeBuffer.append("\"];\n")

            // add egdes
            writeEdges(node, edgeBuffer)
        }
    }

    private def writeEdges(node: Node, edgeBuffer: StringBuffer) {
        // add egdes
        if (configuration.includeEdges)
            for (e ← node.ownEdges) {
                edgeBuffer.append("\t")
                edgeBuffer.append(e.source.uniqueID)
                edgeBuffer.append(" -> ")
                edgeBuffer.append(e.target.uniqueID)
                edgeBuffer.append("[label=\"")
                edgeBuffer.append(e.dType.toString)
                edgeBuffer.append(" [")
                edgeBuffer.append(e.count)
                edgeBuffer.append("]\"];\n")
            }
    }
}

trait DOTClusteringResultWriterConfiguration {
    val includeSingleNodes: Boolean = true
    val includeEdges: Boolean = true
}
