package de.tud.cs.st.clusters.dependency
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
import de.tud.cs.st.bat.dependency.DepGraphExtractor
import de.tud.cs.st.bat.dependency.ClassFileProcessor

@RunWith(classOf[JUnitRunner])
class DepGraphExtractorTest extends Suite with de.tud.cs.st.util.perf.BasicPerformanceEvaluation {

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
  def testDepGraphGeneration() {
    println("testDepGraphGeneration - START")

    val depGraphBuilder = new Graph
    val cfProcessor: ClassFileProcessor = new DepGraphExtractor(depGraphBuilder)

    time(duration => println("process time: " + nanoSecondsToMilliseconds(duration) + "ms")) {
      for ((file, entry) <- testCases.values) {
        var classFile: de.tud.cs.st.bat.resolved.ClassFile = null
        classFile = Java6Framework.ClassFile(() => file.getInputStream(entry))
        //        println(classFile.toXML)
        cfProcessor.process(classFile)
      }
    }
    val fw = new FileWriter("output.dot")
    fw.write(depGraphBuilder.toDot())
    fw.close()

    println("testDepGraphGeneration - END")
  }
}