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
package dependency
import org.scalatest.FunSuite
import java.io.File
import java.util.zip.ZipFile
import java.util.zip.ZipEntry
import de.tud.cs.st.bat.resolved.reader.Java6Framework
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import de.tud.cs.st.clusters.structure.Cluster
import org.junit.Test
import java.io.FileWriter
import de.tud.cs.st.bat.resolved.dependency.DepExtractor
import de.tud.cs.st.clusters.structure.ClusterBuilder
import de.tud.cs.st.bat.resolved.ClassFile

/**
 * @author Thomas Schlosser
 *
 */
@RunWith(classOf[JUnitRunner])
class DepExtractorTest extends AbstractClusteringTest {

  test("testDepGraphGeneration - Apache ANT 1.7.1 - target 1.5.zip") {
    testDepGraohGeneration("test/classfiles/Apache ANT 1.7.1 - target 1.5.zip")
  }

  test("testDepGraphGeneration - ClusteringTestProject.zip") {
    testDepGraohGeneration("test/classfiles/ClusteringTestProject.zip")
  }

  test("testDepGraphGeneration - CommandHistory.class.zip") {
    testDepGraohGeneration("test/classfiles/CommandHistory.class.zip")
  }

  test("testDepGraphGeneration - Flashcards 0.4 - target 1.6.zip") {
    testDepGraohGeneration("test/classfiles/Flashcards 0.4 - target 1.6.zip")
  }

  test("testDepGraphGeneration - hibernate-core-3.6.0.Final.jar") {
    testDepGraohGeneration("test/classfiles/hibernate-core-3.6.0.Final.jar")
  }

  private def testDepGraohGeneration(zipFile: String) {
    println("testDepGraphGeneration[" + zipFile + "] - START")

    val clusterBuilder = new ClusterBuilder
    val depExtractor = new DepExtractor(clusterBuilder)

    var testClasses: Array[ClassFile] = null

    time(duration => println("time to read class files: " + nanoSecondsToMilliseconds(duration) + "ms")) {
      testClasses = getTestClasses(zipFile)
    }

    time(duration => println("time to extract dependencies: " + nanoSecondsToMilliseconds(duration) + "ms")) {
      for (classFile <- testClasses) {
        depExtractor.process(classFile)
      }
    }
    time(duration => println("time to write dot file: " + nanoSecondsToMilliseconds(duration) + "ms")) {
      val fw = new FileWriter(new File(zipFile).getName() + ".dot")
      fw.write(clusterBuilder.getCluster.toDot)
      fw.close()
    }

    println("testDepGraphGeneration[" + zipFile + "] - END")
  }
}