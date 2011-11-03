package de.tud.cs.st.clusters.filter
import org.scalatest.Suite
import java.io.File
import java.util.zip.ZipFile
import java.util.zip.ZipEntry
import org.scalatest.Reporter
import org.scalatest.Stopper
import org.scalatest.Tracker
import org.scalatest.events.TestStarting
import de.tud.cs.st.bat.resolved.reader.Java6Framework
import org.scalatest.events.TestSucceeded
import org.scalatest.events.TestFailed
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import de.tud.cs.st.bat.dependency.DepGraphBuilder
import de.tud.cs.st.clusters.structure.Graph
import org.junit.After
import org.junit.Test
import java.io.FileWriter
import de.tud.cs.st.bat.dependency.ClassFileProcessor
import de.tud.cs.st.bat.dependency.DepGraphExtractor

@RunWith(classOf[JUnitRunner])
class HyperClusterFilterTest extends Suite with de.tud.cs.st.util.perf.BasicPerformanceEvaluation {

  /*
	 * Registry of all class files stored in the zip files found in the test data directory.
	 */
  private val testCases = {

    var tcs = scala.collection.immutable.Map[String, (ZipFile, ZipEntry)]()

    // The location of the "test/data" directory depends on the current directory used for 
    // running this test suite... i.e. whether the current directory is the directory where
    // this class / this source file is stored or the OPAL root directory. 
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

  @Test
  def testHyperClusterFiltering() {
    println("testHyperClusterFiltering - START")

    val depGraphBuilder = new Graph
    val cfProcessor: ClassFileProcessor = new DepGraphExtractor(depGraphBuilder)

    time(duration => println("process time: " + nanoSecondsToMilliseconds(duration) + "ms")) {
      for ((file, entry) <- testCases.values) {
        var classFile: de.tud.cs.st.bat.resolved.ClassFile = null
        classFile = Java6Framework.ClassFile(() => file.getInputStream(entry))
        cfProcessor.process(classFile)
      }
    }

    val filter = new TestHyperClusterFilter with HyperClusterFilter
    var hyperClusters = filter.filter(Array(depGraphBuilder), null)
    println("number of hyper clusters: " + hyperClusters.length)
    hyperClusters.foreach(c => {
      println("write cluster[" + c.name + "] into dot file")
      val fw = new FileWriter(c.name + ".dot")
      fw.write(c.toDot())
      fw.close()
    })

    println("testHyperClusterFiltering - END")
  }

  class TestHyperClusterFilter extends ClusterFilter {
    override def filter(clusters: Array[Graph], projectRootDir: Dir): Array[Graph] = {
      return clusters
    }
  }
}