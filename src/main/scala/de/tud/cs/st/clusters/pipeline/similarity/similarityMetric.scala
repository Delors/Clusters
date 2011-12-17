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
package similarity

import scala.collection.mutable.Map
import ContingencyTableCalculator._
import framework.structure.Cluster
import framework.structure.Node
import de.tud.cs.st.bat.resolved.dependency.DependencyType._
import ContingencyTableCalculator._;

/**
 * @author Thomas Schlosser
 *
 */
trait SimilarityMetric {

    type FeaturesMap = Map[Node, Features]
    type Features = Map[DependencyType, Int]
    type Similarities = Map[(Int, Int), Double]

    /**
     * Calculates similarities between all given nodes.
     *
     * Assumption: sim(a,b)=sim(b,a)
     * Hence, each node pair is considered only once.
     *
     * @param features
     * @return
     */
    def calcSimilarities(features: FeaturesMap): Similarities = {
        var result = Map[(Int, Int), Double]()
        for ((nodeX, featuresX) ← features) {
            for ((nodeY, featuresY) ← features if nodeX.uniqueID < nodeY.uniqueID) {
                result((nodeX.uniqueID, nodeY.uniqueID)) = calcSimilarity(featuresX, featuresY)
            }
        }
        result
    }

    def calcSimilarity(featuresX: Features, featuresY: Features): Double

}

trait BinarySimilarityMetric extends SimilarityMetric {

}

object JacardMetric extends BinarySimilarityMetric {

    override def calcSimilarity(featuresX: Features, featuresY: Features): Double = {
        val a = calcA(featuresX, featuresY)
        val b = calcB(featuresX, featuresY)
        val c = calcC(featuresX, featuresY)
        a.toDouble / (a + b + c)
    }

}
