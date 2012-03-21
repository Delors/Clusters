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

import structure.Cluster
import structure.TypeNode
import structure.FieldNode
import structure.MethodNode
import structure.SourceElementNode

/**
 *
 * @author Thomas Schlosser
 *
 */
trait ClusterStatistics {

    def printStatistics(cluster: Cluster) {
        var subclusterCounter = 0
        var typeCounter = 0
        var fieldCounter = 0
        var methodCounter = 0
        var availableTypeCounter = 0
        var availableFieldCounter = 0
        var availableMethodCounter = 0

        def calcStatistics(c: Cluster) {
            c.nodes foreach {
                case subCluster: Cluster ⇒
                    subclusterCounter += 1
                    calcStatistics(subCluster)
                case TypeNode(_, _, Some(_)) ⇒
                    typeCounter += 1
                    availableTypeCounter += 1
                case TypeNode(_, _, None) ⇒
                    typeCounter += 1
                case FieldNode(_, _, Some(_)) ⇒
                    fieldCounter += 1
                    availableFieldCounter += 1
                case FieldNode(_, _, None) ⇒
                    fieldCounter += 1
                case MethodNode(_, _, Some(_)) ⇒
                    methodCounter += 1
                    availableMethodCounter += 1
                case MethodNode(_, _, None) ⇒
                    methodCounter += 1
            }
        }
        calcStatistics(cluster)

        var sourceElementCounter = typeCounter + fieldCounter + methodCounter
        var availableSourceElementCounter = availableTypeCounter + availableFieldCounter + availableMethodCounter

        println("## Statistics of cluster["+cluster.identifier+"]:")
        println("Number of Types (available/not available): "+typeCounter+"("+availableTypeCounter+"/"+(typeCounter - availableTypeCounter)+")")
        println("Number of Fields (available/not available): "+fieldCounter+"("+availableFieldCounter+"/"+(fieldCounter - availableFieldCounter)+")")
        println("Number of Methods (available/not available): "+methodCounter+"("+availableMethodCounter+"/"+(methodCounter - availableMethodCounter)+")")
        println("Number of Source Elements (available/not available): "+sourceElementCounter+"("+availableSourceElementCounter+"/"+(sourceElementCounter - availableSourceElementCounter)+")")
        println("Number of (Sub-)Cluster: "+subclusterCounter)
    }

    private def calcMaxDepth(cluster: Cluster): Int = {
        var maxDpth = 0
        cluster.nodes foreach {
            case subCl: Cluster ⇒
                maxDpth = scala.math.max(maxDpth, calcMaxDepth(subCl))
            case sen: SourceElementNode ⇒
        }
        return maxDpth + 1
    }
}

object ClusterStatistics extends ClusterStatistics
