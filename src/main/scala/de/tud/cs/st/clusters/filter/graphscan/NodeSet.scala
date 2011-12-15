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

import java.util.ArrayDeque

/**
 * NodeSet is a structure to store IDs of nodes.
 * This structure can either be used as as stack or queue
 *
 * @param isStack Signals whether the internal structure is a stack or queue.
 *
 * @author Thomas Schlosser
 *
 */
class NodeSet(val isStack: Boolean) {

  private val content = new ArrayDeque[Int];

  /**
   * Adds the given Value to the {@link NodeSet}.
   *
   * @param value The value that should be added.
   */
  def add(value: Int) {
    if (isStack)
      content.push(value);
    else
      content.add(value);
  }

  /**
   * Gets the next (first) element.
   *
   * @return value of the next element or <code>-1</code> if the {@link NodeSet} is empty.
   */
  def getNext: Int = {
    if (content.isEmpty())
      return -1;
    return content.peek();
  }

  /**
   * Removes the first value of the {@link NodeSet}.
   */
  def remove {
    content.remove();
  }

  /**
   * Signals whether the {@link NodeSet} is empty.
   *
   * @return <code>true</code> if it is empty.<br/>
   *         <code>false</code> if it is not empty.
   */
  def isEmpty: Boolean =
    content.isEmpty();
}