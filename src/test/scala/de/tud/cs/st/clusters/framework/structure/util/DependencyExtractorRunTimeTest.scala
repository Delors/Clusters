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
package structure
package util

import scala.math.Ordering
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import _root_.de.tud.cs.st.bat.resolved.reader.Java6Framework
import _root_.de.tud.cs.st.bat.resolved.ClassFile

/**
 * @author Thomas Schlosser
 *
 */
@RunWith(classOf[JUnitRunner])
class DepExtractorRunTimeTest extends AbstractClusteringTest {

    test("testDepExtraction - Apache ANT 1.7.1 - target 1.5.zip") {
        testDepExtraction("test/classfiles/Apache ANT 1.7.1 - target 1.5.zip")
    }

    test("testDepExtraction - ClusteringTestProject.zip") {
        testDepExtraction("test/classfiles/ClusteringTestProject.zip")
    }

    test("testDepExtraction - Flashcards 0.4 - target 1.6.zip") {
        testDepExtraction("test/classfiles/Flashcards 0.4 - target 1.6.zip")
    }

    test("testDepExtraction - hibernate-core-3.6.0.Final.jar") {
        testDepExtraction("test/classfiles/hibernate-core-3.6.0.Final.jar")
    }

    test("testDepExtraction - cocome-impl-classes.jar") {
        testDepExtraction("test/classfiles/cocome-impl-classes.jar")
    }

    private def testDepExtraction(zipFile: String) {
        println("testDepExtraction["+zipFile+"]")

        var testClasses = Java6Framework.ClassFiles(zipFile)
        var min = Long.MaxValue
        var max = Long.MinValue
        for (i ← 1 to 10) {
            val depExtractor = new DefaultClusterManager()
            time(duration ⇒ { min = Ordering[Long].min(duration, min); max = Ordering[Long].max(duration, max) }) {
                for (classFile ← testClasses) {
                    depExtractor.process(classFile)
                }
            }
        }
        println("min time to extract dependencies: "+nanoSecondsToMilliseconds(min)+"ms")
        println("max time to extract dependencies: "+nanoSecondsToMilliseconds(max)+"ms")

        println()
    }
}