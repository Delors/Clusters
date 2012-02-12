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
import pipeline.GraphmlClusteringResultWriter
import pipeline.GraphmlClusteringResultWriterConfiguration

/**
 * @author Thomas Schlosser
 *
 */
object ReferenceClusterApp
        extends ConsoleParameterValidator
        with DependencyExtractionUtils
        with ReferenceClusterCreator
        with SourceElementIdentifiersToFile {

    private val defaultOutputFileExtension = ".sei"

    private val usage = """
Usage: ReferenceClusterApp [-mode [write|read|default]] -binaryFile <binaryFile> [-options]
   or  ReferenceClusterApp -h | --help | -?
           (to print this help message)

where mode implies:
    write        Writes all identifiers of the source elements given in the binary file into
                 the given or default output file.
                 # required options of this mode: none
                 # further options of this mode:
                   -outputFile
    read         Reads the cluster structure of the reference cluster file and create
                 the corresponding graphml output file.
                 # required options of this mode:
                   -referenceClusterFile
                 # further options of this mode:
                   -graphmlFile
    default      Combines the write and read mode. After the identifiers have been written to
                 the output file, the cluster structure can be modified while the application
                 waits on <enter>-key interruption. Finally, the corresponding graphml file will
                 be created.
                 # required options of this mode: none
                 # further options of this mode:
                   -outputFile
                   -graphmlFile
        
where options include:
    -outputFile <filepath>
                  Path of the file, where the identifiers are written to.
                  Default value is the <binaryFile> value with extension changed to '."""+defaultOutputFileExtension+"""'.
                  If the default file already exists, the next free '.<number>."""+defaultOutputFileExtension+"""' extension is used.
    -graphmlFile <filepath>
                  Path of the graphml output file.
                  Default value is the <binaryFile> value with extension changed to 'graphml'.
    -referenceClusterFile <filepath>
                  Path to the file that contains the reference cluster structure in the form:
                  <cluster> ::=
                  [
                  <identifier|cluster>
                  ]
"""

    private val modeMap = Map("default" -> 0, "write" -> 1, "read" -> 2)

    def main(args: Array[String]) {
        if (args.length == 0 ||
            (args.length == 1 && (args(0) == "-h" || args(0) == "--help" || args(0) == "-?"))) {
            println(usage)
            sys.exit(0)
        }
        if (args.length == 1) {
            println("The given parameter is unknown.\nPlease use 'ReferenceClusterApp -h' to get help.")
            sys.exit(1)
        }

        var nextArgToProcess = 0
        var mode = modeMap("default")
        if (args(0) == "-mode") {
            modeMap.get(args(1)) match {
                case Some(modeId) ⇒ mode = modeId
                case None ⇒
                    println("The given mode is unknown.\nPlease use 'ReferenceClusterApp -h' to get help.")
                    sys.exit()
            }
            nextArgToProcess = 2
        }

        if (args.length < nextArgToProcess + 2 || args(nextArgToProcess) != "-binaryFile") {
            println("The required parameter '-binaryFile' is missing.\nPlease use 'ReferenceClusterApp -h' to get help.")
            sys.exit(1)
        }

        val binaryFilePath = args(nextArgToProcess + 1)

        validateInputFileParameter(new File(binaryFilePath), "-binaryFile")

        val arglist = args.drop(nextArgToProcess + 2).toList

        var outputFile: Option[File] = None
        var graphmlFile: Option[File] = None
        var referenceClusterFile: Option[File] = None

        def parseOptions(list: List[String]) {
            list match {
                case "-outputFile" :: value :: tail ⇒ {
                    if (outputFile.isDefined) {
                        println("Option '-outputFile' is defined multiple times.\nPlease use 'ReferenceClusterApp -h' to get help.")
                        sys.exit(1)
                    }
                    val file = new File(value)
                    createNonExistentFile(file)
                    validateOutputFileParameter(file, "-outputFile")
                    outputFile = Some(file)
                    parseOptions(tail)
                }
                case "-graphmlFile" :: value :: tail ⇒
                    if (graphmlFile.isDefined) {
                        println("Option '-graphmlFile' is defined multiple times.\nPlease use 'ReferenceClusterApp -h' to get help.")
                        sys.exit(1)
                    }
                    val file = new File(value)
                    createNonExistentFile(file)
                    validateOutputFileParameter(file, "-graphmlFile")
                    graphmlFile = Some(file)
                    parseOptions(tail)
                case "-referenceClusterFile" :: value :: tail ⇒
                    if (referenceClusterFile.isDefined) {
                        println("Option '-referenceClusterFile' is defined multiple times.\nPlease use 'ReferenceClusterApp -h' to get help.")
                        sys.exit(1)
                    }
                    val file = new File(value)
                    validateInputFileParameter(file, "-referenceClusterFile")
                    referenceClusterFile = Some(file)
                    parseOptions(tail)
                case option :: tail ⇒
                    println("Unknown option "+option+"\nPlease use 'ReferenceClusterApp -h' to get help.")
                    sys.exit(1)
                case Nil ⇒ //nothing to do at the end of the options list
            }
        }
        parseOptions(arglist)

        mode match {
            case 0 ⇒ { // default
                val concreteOutputFile = getConcreteFile(binaryFilePath, outputFile, defaultOutputFileExtension)
                val concreteGraphmlFile = getConcreteFile(binaryFilePath, graphmlFile, "graphml", false)
                runWriteMode(binaryFilePath, concreteOutputFile)
                println("Please press enter to continue after you have finished modifications of the 'outputFile' (\""+concreteOutputFile.getAbsolutePath()+"\").")
                System.in.read
                runReadMode(binaryFilePath, concreteOutputFile, concreteGraphmlFile)
            }
            case 1 ⇒ { // write
                runWriteMode(binaryFilePath, getConcreteFile(binaryFilePath, outputFile, defaultOutputFileExtension))
            }
            case 2 ⇒ { // read
                if (referenceClusterFile.isEmpty) {
                    println("The required parameter '-referenceClusterFile' is missing.\nPlease use 'ReferenceClusterApp -h' to get help.")
                    sys.exit(1)
                }

                val concreteGraphmlFile = getConcreteFile(binaryFilePath, graphmlFile, "graphml", false)

                runReadMode(binaryFilePath, referenceClusterFile.get, concreteGraphmlFile)
            }
        }
    }

    private def runWriteMode(binaryFilePath: String, outputFile: File) {
        writeSourceElementsToFile(SourceZipFile(binaryFilePath), outputFile)
    }

    private def runReadMode(binaryFilePath: String, referenceClusterFile: File, graphmlFile: File) {
        try {
            val cluster = readReferenceCluster(SourceZipFile(binaryFilePath), referenceClusterFile)

            val writerConfig = new {
                override val aggregateEdges: Boolean = true
                override val showEdgeLabels: Boolean = false
                override val showSourceElementNodes: Boolean = false
                override val maxNumberOfLevels: Option[Int] = None
            } with GraphmlClusteringResultWriterConfiguration

            val clusterWriter = new GraphmlClusteringResultWriter(graphmlFile.getAbsolutePath(), writerConfig)
            try {
                clusterWriter.write(cluster)
            }
            finally {
                clusterWriter.close()
            }
        }
        catch {
            case e: Exception ⇒
                println(e.getMessage())
                sys.exit(1)
        }
    }

    private def getConcreteFile(
        binaryFilePath: String,
        optFile: Option[File],
        fileExtension: String,
        ensureNewFile: Boolean = true): File = {
        if (optFile.isEmpty) {
            var defaultFile: File = null
            val fileNameWithoutExt = binaryFilePath.replaceFirst("[.][^.]+$", "");
            var createdNewFile = false
            var counter = 0
            while (!createdNewFile) {
                val defaultFileName = fileNameWithoutExt + { if (counter != 0) "."+counter else "" }+"."+fileExtension
                defaultFile = new File(defaultFileName)
                createdNewFile = defaultFile.createNewFile()
                createdNewFile ||= !ensureNewFile
                counter += 1
            }
            defaultFile
        }
        else
            optFile.get
    }

    private def createNonExistentFile(file: File) {
        if (file != null && !file.exists)
            file.createNewFile()
    }
}