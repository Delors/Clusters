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
package pipeline

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import framework.AbstractClusteringTest
import framework.structure.util.ClusterBuilder

/**
 * @author Thomas Schlosser
 *
 */
@RunWith(classOf[JUnitRunner])
class CombinedClusteringTest extends AbstractClusteringTest {

    //    implicit val clustering = (builder: ClusterBuilder) ⇒
    //        InternExternClustering(builder,
    //            newClusterClustering = GetterSetterClustering(builder,
    //                StronglyConnectedComponentsClustering(builder,
    //                    SingleElementClusterRemover(builder,
    //                        LayerClustering(builder)))))
    implicit val clustering = (builder: ClusterBuilder) ⇒
        InternalExternalClustering(builder,
            internalClustering = InternalClassClustering(builder)) //,
    //            newClusterClustering = LayerClustering(builder, true))

    test("testCombinedClustering - ClusteringTestProject.zip") {
        testClustering(
            "testCombinedClustering - ClusteringTestProject.zip",
            extractDependencies("test/classfiles/ClusteringTestProject.zip"),
            Some("ClusteringTestProject"),
            includeSingleNodes = true,
            includeEdges = true)
    }

    test("testCombinedClustering - cocome-impl-classes.jar") {
        testClustering(
            "testCombinedClustering - cocome-impl-classes.jar",
            extractDependencies("test/classfiles/cocome-impl-classes.jar"),
            Some("cocome-impl-classes"),
            includeSingleNodes = true,
            includeEdges = true)
    }

    //    test("testCombinedClustering - Flashcards 0.4 - target 1.6.zip") {
    //        testClustering(
    //            "testCombinedClustering - Flashcards 0.4 - target 1.6.zip",
    //            extractDependencies("test/classfiles/Flashcards 0.4 - target 1.6.zip"),
    //            Some("Flashcards 0.4 - target 1.6"),
    //            includeSingleNodes = true,
    //            includeEdges = false)
    //    }
}
