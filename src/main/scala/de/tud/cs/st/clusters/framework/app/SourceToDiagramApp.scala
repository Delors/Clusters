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
package app

import structure.util.DefaultDependencyExtractor
import util.SourceZipFile
import util.DependencyExtractionUtils
import framework.pipeline.result.GraphmlClusteringResultWriter
import framework.pipeline.result.GraphmlClusteringResultWriterConfiguration

/**
 * This application can be used to create a diagram of a software project's dependency graph.
 *
 * @author Thomas Schlosser
 *
 */
object SourceToDiagramApp {

    private val usage = """
Usage: SourceToDiagramApp <inputFile> <outputFile>
   or  SourceToDiagramApp -h | --help | -?
           (to print this help message)
"""

    def main(args: Array[String]) {
        if (args.length == 0 ||
            (args.length == 1 && (args(0) == "-h" || args(0) == "--help" || args(0) == "-?"))) {
            println(usage)
            sys.exit(0)
        }
        if (args.length == 1) {
            println("The given parameter is unknown.\nPlease use 'SourceToDiagramApp -h' to get help.")
            sys.exit(1)
        }
        if (args.length > 2) {
            println("Too many parameters are given.\nPlease use 'SourceToDiagramApp -h' to get help.")
            sys.exit(1)
        }

        val sourceZipFile = SourceZipFile(args(0))

        val dependencyExtractor = new DefaultDependencyExtractor()
        DependencyExtractionUtils.extractDependencies(dependencyExtractor)(sourceZipFile)

        val writerConfiguration = new {
            override val aggregateEdges = false
            override val showEdgeLabels = true
            override val showSourceElementNodes = true
            override val maxNumberOfLevels = None
        } with GraphmlClusteringResultWriterConfiguration
        val resultWriter = new GraphmlClusteringResultWriter(args(1), writerConfiguration)

        try {
            resultWriter.write(dependencyExtractor.clusterManager.getProjectCluster)
        }
        finally {
            resultWriter.close()
        }
    }
}