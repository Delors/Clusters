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
package util

import java.io.File
import java.io.FileReader
import java.io.BufferedReader
import pipeline.GraphmlClusteringResultWriter
import pipeline.GraphmlClusteringResultWriterConfiguration
import structure.Cluster
import structure.Node
import structure.util.DefaultClusterManager
import de.tud.cs.st.bat.resolved.dependency.DependencyExtractor
import de.tud.cs.st.bat.resolved.reader.Java6Framework
import de.tud.cs.st.bat.resolved.DoNothingSourceElementsVisitor

/**
 * @author Thomas Schlosser
 *
 */
trait ReferenceClusterCreator
        extends DependencyExtractionUtils {

    def readReferenceCluster(sourceInputFilePath: String, referenceClusterInputFile: File): Cluster = {
        val clusterManager = new DefaultClusterManager()
        extractDependencies(sourceInputFilePath)(clusterManager)
        val projectCluster = clusterManager.getProjectCluster
        var identifierMap = Map[String, Node]()
        projectCluster.getNodes foreach { node ⇒
            identifierMap = identifierMap + ((node.identifier, node))
        }
        projectCluster.clearNodes();

        val fr = new FileReader(referenceClusterInputFile)
        val br = new BufferedReader(fr)
        var line: String = br.readLine()
        var lineCounter = 1
        var currentParent: Node = null
        var currentNode: Node = null
        var clusterCounter = 1
        while (line != null) {
            line = line.trim()
            if (line == "[") {
                if (currentParent == null)
                    currentParent = projectCluster
                else {
                    val newParent = clusterManager.createCluster("cluster_"+clusterCounter, "")
                    clusterCounter += 1
                    currentParent.addNode(newParent)
                    currentParent = newParent
                }
            }
            else if (line == "]") {
                if (currentParent == null) {
                    sys.error("Cluster end marker is not allowed in line "+lineCounter+". There is no cluster to close. Please re-check the cluster hierarchy!")
                }
                currentParent = currentParent.parent
            }
            else {
                //line is an identifier
                identifierMap.get(line) match {
                    case Some(node) ⇒ {
                        currentNode = node
                        if (currentParent == null) {
                            sys.error("A cluster has to be started before node '"+line+"' in line "+lineCounter+" can be added.")
                        }
                        else
                            currentParent.addNode(currentNode)
                    }
                    case None ⇒ {
                        sys.error("Line "+lineCounter+" '"+line+"' is not a valid identifier for the given 'sourceInputFile'. Please re-check this line and try it again.")
                    }
                }
            }
            line = br.readLine()
            lineCounter += 1
        }
        if (currentParent != null) {
            sys.error("Reached end of file and not all clusters are closed correctly. Please re-check the cluster hierarchy!")
        }
        br.close()
        projectCluster
    }
}

object ReferenceClusterCreator extends ReferenceClusterCreator