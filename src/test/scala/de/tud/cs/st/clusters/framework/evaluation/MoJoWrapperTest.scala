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
package evaluation

import TestSources._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import de.tud.cs.st.clusters.framework.pipeline.ClusteringStage
import util.ReferenceClusterCreator
import de.tud.cs.st.clusters.pipeline.algorithm.ApplicationLibrariesSeparator
import de.tud.cs.st.clusters.pipeline.algorithm.ApplicationLibrariesSeparatorConfiguration

/**
 * @author Thomas Schlosser
 *
 */
@RunWith(classOf[JUnitRunner])
class MoJoWrapperTest extends AbstractClusteringTest {

    test("test calculation of double direction MojoFM quality value") {
        val referenceClusters = ReferenceClusterCreator.readReferenceCluster(
            clusteringTestProjectSourceZipFile,
            new java.io.File("test/referenceCluster/ClusteringTestProject.sei"))

        val appLibConfig = new ApplicationLibrariesSeparatorConfiguration {}
        val clusteringStages: Array[ClusteringStage] = Array(
            new ApplicationLibrariesSeparator(appLibConfig))

        val extractedClusters = testClustering(
            "testMoJoWrapper-ApplicationLibrariesSeparatorStage [ClusteringTestProject.zip]",
            clusteringTestProjectSourceZipFile)(clusteringStages)

        var mjw = new MoJoWrapper(extractedClusters, referenceClusters)
        println("singleDirectionMoJoHM: "+mjw.singleDirectionMoJoHM)
        println("singleDirectionMoJo: "+mjw.singleDirectionMoJo)
        println("singleDirectionMoJoPlus: "+mjw.singleDirectionMoJoPlus)
        println("singleDirectionMoJoFM: "+mjw.singleDirectionMoJoFM)
        println("doubleDirectionMoJo: "+mjw.doubleDirectionMoJo)
        println("doubleDirectionMoJoPlus: "+mjw.doubleDirectionMoJoPlus)
        println("doubleDirectionMojoFM: "+mjw.doubleDirectionMojoFM)
        //        println("showMoveJoinSequence(): ")
        //        mjw.showMoveJoinSequence()
    }
}
