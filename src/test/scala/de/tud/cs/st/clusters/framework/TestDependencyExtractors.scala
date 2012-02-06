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

import util.DependencyExtractionUtils

/**
 * @author Thomas Schlosser
 *
 */
trait TestDependencyExtractors extends DependencyExtractionUtils {

    // whole zip/jar files
    protected val antDependencyExtractor = extractDependencies("test/classfiles/Apache ANT 1.7.1 - target 1.5.zip")
    protected val clusteringTestProjectDependencyExtractor = extractDependencies("test/classfiles/ClusteringTestProject.zip")
    protected val cocomeDependencyExtractor = extractDependencies("test/classfiles/cocome-impl-classes.jar")
    protected val flashcardsDependencyExtractor = extractDependencies("test/classfiles/Flashcards 0.4 - target 1.6.zip")
    protected val hibernateDependencyExtractor = extractDependencies("test/classfiles/hibernate-core-3.6.0.Final.jar")

    // only selected classes
    protected val getterSetterTestClassDependencyExtractor = extractDependencies("test/classfiles/ClusteringTestProject.zip", "test/GetterSetterTestClass.class")
    protected val stronglyConnectedComponentsTestClassDependencyExtractor = extractDependencies("test/classfiles/ClusteringTestProject.zip", "test/StronglyConnectedComponentsTestClass.class")
    protected val cocomePrintercontrollerDependencyExtractor = extractDependencies("test/classfiles/cocome-impl-classes.jar",
        "org/cocome/tradingsystem/cashdeskline/cashdesk/printercontroller/PrinterControllerEventHandlerIf.class",
        "org/cocome/tradingsystem/cashdeskline/cashdesk/printercontroller/impl/PrinterController.class",
        "org/cocome/tradingsystem/cashdeskline/cashdesk/printercontroller/impl/PrinterControllerEventHandlerImpl.class",
        "org/cocome/tradingsystem/cashdeskline/cashdesk/printercontroller/impl/PrinterStates.class")
}