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
   * Die Entdeckungszeitpunkte der Knoten des Graphen.
   */
  var discoveryTime: Map[Int, Int] = _

  /**
   * Die Finishing-Zeitpunkte der Knoten des Graphen.
   */
  var finishingTime: Map[Int, Int] = _

  /**
   * Die Vorgängerknoten der Knoten des Graphen.
   */
  var pi: Map[Int, Int] = _

  /**
   * Die Einfärbung der Knoten des Graphen.
   */
  var color: Map[Int, Int] = _

  /**
   * Die Knoten des Graphen absteigend nach Finishing-Zeit sortiert.
   */
  var order: Array[Int] = _

  /**
   * Signalisiert ob ein Knoten auf gerader Entfernung zu dem Startknoten
   * (seiner Wurzel) liegt.
   */
  var evenDist: Map[Int, Boolean] = _

  /**
   * Enthält die Anzahl der unbearbeiteten Knoten(zumindest bezogen auf die
   * Sortierung).
   */
  var unfinishedNodes = 0

  /**
   * Die Zeit, bei der der Algorithmus beendet wurde.
   */
  var time: Int = _

  /**
   * Erstellt und setzt alle relevanten Daten, die für die Sortierung nach
   * Finishing-Zeit relevant sind.
   *
   * @param size
   *            Die max-Anzahl der Knoten.
   */
  def createOrderElements(size: Int) {
    order = new Array(size)
    unfinishedNodes = size
  }

  /**
   * Verringert die Anzahl der unbearbeiteten Knoten und gibt dieses Ergebnis
   * zurück.<br/>
   * <b style="color:red">HINWEIS:</b> Bevor diese Methode verwendet werden
   * kann sollte die Methode {@link #createOrderElements(int)} aufgerufen
   * werden um die benötigte Struktur zu erstellen.
   *
   * @return Die Anzahl der unbearbeiteten Knoten.
   */
  def decreaseUnfinishedNodes(): Int = {
    unfinishedNodes -= 1
    unfinishedNodes
  }
}