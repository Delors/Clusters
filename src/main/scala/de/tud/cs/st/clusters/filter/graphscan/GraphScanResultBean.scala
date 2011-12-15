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
package graphscan

import scala.collection.mutable.Map

/**
 * @author Thomas Schlosser
 *
 */
class GraphScanResultBean {
    /**
     * Graph's nodes discovery time.
     */
    var discoveryTime: Map[Int, Int] = _

    /**
     * Graph's nodes finishing time.
     */
    var finishingTime: Map[Int, Int] = _

    /**
     * Graph's nodes predecessor nodes.
     */
    var pi: Map[Int, Int] = _

    /**
     * Graph's nodes color.
     */
    var color: Map[Int, Int] = _

    /**
     * Graph's nodes finishing times sorted in descending order.
     */
    var order: Array[Int] = _

    /**
     * Signals, whether a node has an even or uneven distance to the start (root) node.
     */
    var evenDist: Map[Int, Boolean] = _

    /**
     * The number of unfinished nodes (relating to the nodes' sorting).
     */
    var unfinishedNodes = 0

    /**
     * The time, the algorithm was finished.
     */
    var time: Int = _

    /**
     * Creates and sets all relevant data, that are relevant for sorting of the finishing times.
     *
     * @param size
     *            Maximum number of nodes.
     */
    def createOrderElements(size: Int) {
        order = new Array(size)
        unfinishedNodes = size
    }

    /**
     * Decreases the number of unfinished nodes and returns this result.<br/>
     * <b style="color:red">NOTE:</b> Before using this method, you should call method
     * {@link #createOrderElements(int)} to create the required structure.
     *
     * @return The number of unfinished nodes.
     */
    def decreaseUnfinishedNodes(): Int = {
        unfinishedNodes -= 1
        unfinishedNodes
    }
}