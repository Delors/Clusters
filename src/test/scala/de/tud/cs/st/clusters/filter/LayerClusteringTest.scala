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
package filter

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import framework.AbstractClusteringTest
import framework.filter.IdentityMapClusterFilter
import framework.structure.util.ClusterBuilder
import framework.filter.IdentityMapClusterFilter

/**
 * @author Thomas Schlosser
 *
 */
@RunWith(classOf[JUnitRunner])
class LayerClusteringTest extends AbstractClusteringTest {

    implicit val clustering = (builder: ClusterBuilder) ⇒ LayerClustering(builder)

    test("testLayerClustering [ClusteringTestProject.zip]") {
        testClustering(
            "testLayerClustering [ClusteringTestProject.zip]",
            extractDependencies("test/classfiles/ClusteringTestProject.zip", "test/GetterSetterTestClass.class"),
            Some("ClusteringTestProject_GetterSetterTestClass"))
    }

    test("testLayerClustering [Flashcards 0.4 - target 1.6.zip -- CommandHistory.class]") {
        testClustering(
            "testLayerClustering [Flashcards 0.4 - target 1.6.zip -- CommandHistory.class]",
            extractDependencies("test/classfiles/Flashcards 0.4 - target 1.6.zip", "de/tud/cs/se/flashcards/model/CommandHistory.class"),
            Some("CommandHistory"))
    }

    test("testLayerClustering [Flashcards 0.4 - target 1.6.zip]") {
        testClustering(
            "testLayerClustering [Flashcards 0.4 - target 1.6.zip]",
            extractDependencies("test/classfiles/Flashcards 0.4 - target 1.6.zip"),
            Some("Flashcards 0.4 - target 1.6"))
    }

    test("testLayerClustering [cocome-impl-classes.jar]") {
        testClustering(
            "testLayerClustering [cocome-impl-classes.jar]",
            extractDependencies("test/classfiles/cocome-impl-classes.jar"),
            Some("cocome-impl"))
    }

    test("testLayerClustering [hibernate-core-3.6.0.Final.jar]") {
        testClustering("testLayerClustering [hibernate-core-3.6.0.Final.jar]",
            extractDependencies("test/classfiles/hibernate-core-3.6.0.Final.jar"))
    }
}
