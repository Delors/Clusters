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
package de.tud.cs.st.clusters.filter
import org.scalatest.FunSuite
import java.io.File
import java.util.zip.ZipFile
import java.util.zip.ZipEntry
import de.tud.cs.st.bat.resolved.reader.Java6Framework
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import de.tud.cs.st.bat.resolved.dependency.DepExtractor
import de.tud.cs.st.clusters.structure.ClusterBuilder
import org.junit.Test
import java.io.FileWriter
import de.tud.cs.st.clusters.resolved.BasicClusteringFramework
import de.tud.cs.st.bat.resolved.ClassFile

/**
 * @author Thomas Schlosser
 *
 */
@RunWith(classOf[JUnitRunner])
class HyperClusterFilterTest extends FunSuite with de.tud.cs.st.util.perf.BasicPerformanceEvaluation {

  test("testHyperClusterFiltering") {
    testGetterSetterClustering("testHyperClusterFiltering",
      { de => for (cf <- getTestClasses("test/classfiles/Flashcards 0.4 - target 1.6.zip")) de.process(cf) })
  }

  private def testGetterSetterClustering(testName: String, extractDeps: (DepExtractor) => Unit) {
    println(testName + " - START")

    val clusterBuilder = new ClusterBuilder
    val depExtractor = new DepExtractor(clusterBuilder)

    extractDeps(depExtractor)

    val framework = new BasicClusteringFramework with HyperClusterFilter
    var clusters = framework.filter(Array(clusterBuilder.getCluster), null)
    clusters.foreach(c => {
      println("write cluster[" + c.identifier + "] into dot file")
      val fw = new FileWriter(c.identifier + ".dot")
      fw.write(c.toDot())
      fw.close()
    })

    println(testName + " - END")
  }

  private def getTestClasses(zipFile: String): Array[ClassFile] = {
    var tcls = Array.empty[ClassFile]
    val zipfile = new ZipFile(new File(zipFile))
    val zipentries = (zipfile).entries
    while (zipentries.hasMoreElements) {
      val zipentry = zipentries.nextElement
      if (!zipentry.isDirectory && zipentry.getName.endsWith(".class")) {
        val testClass = (Java6Framework.ClassFile(() => zipfile.getInputStream(zipentry)))
        tcls :+= testClass
      }
    }
    tcls
  }
}
