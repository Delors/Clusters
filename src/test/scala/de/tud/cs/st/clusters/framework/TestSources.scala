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

import util.SourceZipFile

/**
 * @author Thomas Schlosser
 *
 */
trait TestSources {

    // whole zip/jar files
    val antSourceZipFile = SourceZipFile("test/classfiles/Apache ANT 1.7.1 - target 1.5.zip")
    val clusteringTestProjectSourceZipFile = SourceZipFile("test/classfiles/ClusteringTestProject.zip")
    val cocomeSourceZipFile = SourceZipFile("test/classfiles/cocome-impl-classes.jar")
    val flashcardsSourceZipFile = SourceZipFile("test/classfiles/Flashcards 0.4 - target 1.6.zip")
    val hibernateSourceZipFile = SourceZipFile("test/classfiles/hibernate-core-3.6.0.Final.jar")
    def javaRuntimeSourceZipFile = {
        if (!new java.io.File("test/classfiles/rt_jdk1.7.0_01.jar").exists()) {
            sys.error("The optional file 'test/classfiles/rt_jdk1.7.0_01.jar' is not available. Please add this file first if you want to use it!")
        }
        SourceZipFile("test/classfiles/rt_jdk1.7.0_01.jar")
    }

    // only selected classes
    val getterSetterTestClassSourceZipFile = SourceZipFile("test/classfiles/ClusteringTestProject.zip", "test/GetterSetterTestClass.class")
    val stronglyConnectedComponentsTestClassSourceZipFile = SourceZipFile("test/classfiles/ClusteringTestProject.zip", "test/StronglyConnectedComponentsTestClass.class")
    val cocomePrintercontrollerSourceZipFile = SourceZipFile("test/classfiles/cocome-impl-classes.jar",
        "org/cocome/tradingsystem/cashdeskline/cashdesk/printercontroller/PrinterControllerEventHandlerIf.class",
        "org/cocome/tradingsystem/cashdeskline/cashdesk/printercontroller/impl/PrinterController.class",
        "org/cocome/tradingsystem/cashdeskline/cashdesk/printercontroller/impl/PrinterControllerEventHandlerImpl.class",
        "org/cocome/tradingsystem/cashdeskline/cashdesk/printercontroller/impl/PrinterStates.class")

    val clTestProjectExampleTest1SourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-example-test1.jar")
    val clTestProjectPatternAbstractFactorySourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-abstractFactory.jar")
    val clTestProjectPatternAdapterSourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-adapter.jar")
    val clTestProjectPatternBridgeSourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-bridge.jar")
    val clTestProjectPatternBuilderSourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-builder.jar")
    val clTestProjectPatternChainofresponsibilityExample1SourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-chainofresponsibility-example1.jar")
    val clTestProjectPatternChainofresponsibilityExample2SourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-chainofresponsibility-example2.jar")
    val clTestProjectPatternCommandSourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-command.jar")
    val clTestProjectPatternCompositeSourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-composite.jar")
    val clTestProjectPatternDecoratorExample1SourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-decorator-example1.jar")
    val clTestProjectPatternDecoratorExample2SourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-decorator-example2.jar")
    val clTestProjectPatternFacadeSourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-facade.jar")
    val clTestProjectPatternFactorymethodSourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-factorymethod.jar")
    val clTestProjectPatternFlyweightSourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-flyweight.jar")
    val clTestProjectPatternInterpreterSourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-interpreter.jar")
    val clTestProjectPatternIteratorSourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-iterator.jar")
    val clTestProjectPatternMediatorSourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-mediator.jar")
    val clTestProjectPatternMementoSourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-memento.jar")
    val clTestProjectPatternObserverSourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-observer.jar")
    val clTestProjectPatternPrototypeSourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-prototype.jar")
    val clTestProjectPatternProxySourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-proxy.jar")
    val clTestProjectPatternSingletonSourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-singleton.jar")
    val clTestProjectPatternStateSourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-state.jar")
    val clTestProjectPatternStrategySourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-strategy.jar")
    val clTestProjectPatternTemplatemethodSourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-templatemethod.jar")
    val clTestProjectPatternVisitorSourcesZipFile = SourceZipFile("test/classfiles/clusteringTestProject/ClusteringTestProject-pattern-visitor.jar")
}

object TestSources extends TestSources