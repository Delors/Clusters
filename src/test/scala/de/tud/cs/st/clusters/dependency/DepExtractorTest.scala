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
package de.tud.cs.st.clusters.dependency
import org.scalatest.FunSuite
import java.io.File
import java.util.zip.ZipFile
import java.util.zip.ZipEntry
import de.tud.cs.st.bat.resolved.reader.Java6Framework
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import de.tud.cs.st.clusters.structure.Graph
import org.junit.Test
import java.io.FileWriter
import de.tud.cs.st.bat.resolved.dependency.DepExtractor

/**
 * @author Thomas Schlosser
 *
 */
@RunWith(classOf[JUnitRunner])
class DepExtractorTest extends FunSuite with de.tud.cs.st.util.perf.BasicPerformanceEvaluation {

  /*
   * Registry of all class files stored in the zip files found in the test data directory.
   */
  private val testCases = {

    var tcs = scala.collection.immutable.Map[String, (ZipFile, ZipEntry)]()

    // The location of the "test/classfiles" directory depends on the current directory used for 
    // running this test suite... i.e. whether the current directory is the directory where
    // this class / this source file is stored or the Clusters root directory. 
    var files = new File("../../../../../../../test/classfiles").listFiles()
    if (files == null) files = new File("test/classfiles").listFiles()

    for {
      file <- files
      if (file.isFile && file.canRead && file.getName.endsWith(".zip"))
    } {
      val zipfile = new ZipFile(file)
      val zipentries = (zipfile).entries
      while (zipentries.hasMoreElements) {
        val zipentry = zipentries.nextElement
        if (!zipentry.isDirectory && zipentry.getName.endsWith(".class")) {
          val testCase = ("Read class file: " + zipfile.getName + " - " + zipentry.getName -> (zipfile, zipentry))
          tcs = tcs + testCase
        }
      }
    }

    tcs
  }

  test("testDepGraphGeneration") {
    println("testDepGraphGeneration - START")

    val graph = new Graph
    val depExtractor = new DepExtractor(graph)

    time(duration => println("process time: " + nanoSecondsToMilliseconds(duration) + "ms")) {
      for ((file, entry) <- testCases.values) {
        var classFile: de.tud.cs.st.bat.resolved.ClassFile = null
        classFile = Java6Framework.ClassFile(() => file.getInputStream(entry))
        //        println(classFile.toXML)
        depExtractor.process(classFile)
      }
    }
    val fw = new FileWriter("output.dot")
    fw.write(graph.toDot())
    fw.close()

    println("testDepGraphGeneration - END")
  }
}