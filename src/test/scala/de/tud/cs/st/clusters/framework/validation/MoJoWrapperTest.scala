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
package validation

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import structure.Cluster
import structure.SourceElementNode
import mojo.MoJoCalculator
import de.tud.cs.st.clusters.pipeline.InternalClassClusteringStage
import de.tud.cs.st.clusters.pipeline.InternalExternalClusteringStage
import framework.pipeline.ClusteringStage

/**
 * @author Thomas Schlosser
 *
 */
@RunWith(classOf[JUnitRunner])
class MoJoWrapperTest extends AbstractClusteringTest {

    test("calculate double direction MojoFM quality value") {
        val clusteringA: Array[ClusteringStage] = Array(InternalClassClusteringStage())

        val clustersA = testClustering(
            "testInternalClassClusteringStage [ClusteringTestProject.zip]",
            extractDependencies("test/classfiles/ClusteringTestProject.zip"),
            Some("clusterA"))(clusteringA)

        val clusteringB: Array[ClusteringStage] = Array(InternalExternalClusteringStage())

        val clustersB = testClustering(
            "testInternalExternalClusteringStage [ClusteringTestProject.zip]",
            extractDependencies("test/classfiles/ClusteringTestProject.zip"),
            Some("clusterB"))(clusteringB)

        println("MoJo:")
        var mjw = new MoJoWrapper(clustersA, clustersB)
        println(mjw.doubleDirectionMojoFM)
    }
}
