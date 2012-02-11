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
package pipeline

import structure.Node
import structure.Cluster
import structure.SourceElementNode
import org.apache.commons.lang3.StringEscapeUtils

/**
 * @author Thomas Schlosser
 *
 */
class GraphmlClusteringResultWriter(
    val graphmlFileName: String,
    val configuration: GraphmlClusteringResultWriterConfiguration)
        extends ClusteringResultFileWriter(graphmlFileName, "graphml") {

    var nextEdgeID = 0
    var nextSubgraphID = 0
    var aggregatedEdgesSet = Set[(Int, Int)]()

    override protected def writeHeader() {
        write("""<?xml version="1.0" encoding="UTF-8" standalone="no"?>
  <graphml xmlns="http://graphml.graphdrawing.org/xmlns" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:y="http://www.yworks.com/xml/graphml" xmlns:yed="http://www.yworks.com/xml/yed/3" xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd">
  <!--Created by Clusters for Java 2.8-->
    <key for="graphml" id="d0" yfiles.type="resources"/>
    <key for="port" id="d1" yfiles.type="portgraphics"/>
    <key for="port" id="d2" yfiles.type="portgeometry"/>
    <key for="port" id="d3" yfiles.type="portuserdata"/>
    <key attr.name="url" attr.type="string" for="node" id="d4"/>
    <key attr.name="description" attr.type="string" for="node" id="d5"/>
    <key for="node" id="d6" yfiles.type="nodegraphics"/>
    <key attr.name="url" attr.type="string" for="edge" id="d7"/>
    <key attr.name="description" attr.type="string" for="edge" id="d8"/>
    <key for="edge" id="d9" yfiles.type="edgegraphics"/>
    <graph edgedefault="directed" id="G">
""")
    }

    override protected def writeFooter() {
        write("""  </graph>
  <data key="d0">
    <y:Resources/>
  </data>
</graphml>""")
    }

    override protected def writeCluster(cluster: Cluster, nodeBuffer: StringBuffer, edgeBuffer: StringBuffer) {
        // Example:
        //    <node id="n0" yfiles.foldertype="group">
        //      <data key="d5"/>
        //      <data key="d6">
        //        <y:ProxyAutoBoundsNode>
        //          <y:Realizers active="0">
        //            <y:GroupNode>
        //              <y:Fill color="#F8ECC9" transparent="false"/>
        //              <y:BorderStyle color="#000000" type="line" width="1.0"/>
        //              <y:NodeLabel alignment="right" autoSizePolicy="node_width" backgroundColor="#404040" borderDistance="0.0" fontFamily="Dialog" fontSize="16" fontStyle="plain" hasLineColor="false" height="22.625" modelName="internal" modelPosition="t" textColor="#FFFFFF" visible="true" width="3260.63639027429" x="0.0" y="0.0">project</y:NodeLabel>
        //              <y:Shape type="rectangle3d"/>
        //              <y:State closed="false" innerGraphDisplayEnabled="false"/>
        //              <y:Insets bottom="15" bottomF="15.0" left="15" leftF="15.0" right="15" rightF="15.0" top="15" topF="15.0"/>
        //              <y:BorderInsets bottom="1" bottomF="1.0" left="0" leftF="0.0" right="1" rightF="1.0" top="0" topF="0.0"/>
        //            </y:GroupNode>
        //            <y:GroupNode>
        //              <y:Fill color="#F8ECC9" transparent="false"/>
        //              <y:BorderStyle color="#000000" type="line" width="1.0"/>
        //              <y:NodeLabel alignment="right" autoSizePolicy="node_width" backgroundColor="#404040" borderDistance="0.0" fontFamily="Dialog" fontSize="16" fontStyle="plain" hasLineColor="false" height="22.625" modelName="internal" modelPosition="t" textColor="#FFFFFF" visible="true" width="3263.636474609375" x="0.0" y="0.0">project</y:NodeLabel>
        //              <y:Shape type="rectangle3d"/>
        //              <y:State closed="true" innerGraphDisplayEnabled="false"/>
        //              <y:Insets bottom="15" bottomF="15.0" left="15" leftF="15.0" right="15" rightF="15.0" top="15" topF="15.0"/>
        //              <y:BorderInsets bottom="0" bottomF="0.0" left="0" leftF="0.0" right="0" rightF="0.0" top="0" topF="0.0"/>
        //            </y:GroupNode>
        //          </y:Realizers>
        //        </y:ProxyAutoBoundsNode>
        //      </data>
        //      <graph edgedefault="directed" id="n0:">
        //      ...
        //      </graph>
        //    </node>

        if (isVisibleLevel(cluster.level)) {
            write("    <node id=\""+cluster.uniqueID+"\" yfiles.foldertype=\"group\">\n")
            // add description in all cases where the next level is not visible
            // OR the next (intermediate) level contains source element nodes that
            // will not be shown in the resulting graph
            if (configuration.showSourceElementNodes && (
                configuration.maxNumberOfLevels.isEmpty ||
                configuration.maxNumberOfLevels.get != cluster.level)) {
                write("      <data key=\"d5\"/>\n")
            }
            else {
                write("      <data key=\"d5\"><![CDATA[")
                if (configuration.maxNumberOfLevels.isDefined &&
                    configuration.maxNumberOfLevels.get == cluster.level) {
                    // write identifiers of all child nodes (also these of the grandchildren)
                    writeAllChildNodeIdentifiers(cluster)
                }
                else {
                    cluster.nodes foreach { child ⇒
                        if (!child.isCluster) {
                            write(child.identifier+"\n")
                        }
                    }
                }
                write("]]></data>\n")
            }

            write("""      <data key="d6">
        <y:ProxyAutoBoundsNode>
            <y:Realizers active="0">
            <y:GroupNode>
              <y:Fill color="#F8ECC9" transparent="false"/>
              <y:BorderStyle color="#000000" type="line" width="1.0"/>
              <y:NodeLabel alignment="right" autoSizePolicy="node_width" backgroundColor="#404040" borderDistance="0.0" fontFamily="Dialog" fontSize="16" fontStyle="plain" hasLineColor="false" height="22.625" modelName="internal" modelPosition="t" textColor="#FFFFFF" visible="true" width="3260.63639027429" x="0.0" y="0.0">""")
            write(StringEscapeUtils.escapeXml(cluster.identifier))
            write("""</y:NodeLabel>
              <y:Shape type="rectangle3d"/>
               <y:State closed="false" innerGraphDisplayEnabled="false"/>
              <y:Insets bottom="15" bottomF="15.0" left="15" leftF="15.0" right="15" rightF="15.0" top="15" topF="15.0"/>
              <y:BorderInsets bottom="1" bottomF="1.0" left="0" leftF="0.0" right="1" rightF="1.0" top="0" topF="0.0"/>
            </y:GroupNode>
            <y:GroupNode>
              <y:Fill color="#F8ECC9" transparent="false"/>
              <y:BorderStyle color="#000000" type="line" width="1.0"/>
              <y:NodeLabel alignment="right" autoSizePolicy="node_width" backgroundColor="#404040" borderDistance="0.0" fontFamily="Dialog" fontSize="16" fontStyle="plain" hasLineColor="false" height="22.625" modelName="internal" modelPosition="t" textColor="#FFFFFF" visible="true" width="3263.636474609375" x="0.0" y="0.0">""")
            write(StringEscapeUtils.escapeXml(cluster.identifier))
            write("""</y:NodeLabel>
              <y:Shape type="rectangle3d"/>
              <y:State closed="true" innerGraphDisplayEnabled="false"/>
              <y:Insets bottom="15" bottomF="15.0" left="15" leftF="15.0" right="15" rightF="15.0" top="15" topF="15.0"/>
              <y:BorderInsets bottom="0" bottomF="0.0" left="0" leftF="0.0" right="0" rightF="0.0" top="0" topF="0.0"/>
            </y:GroupNode>
          </y:Realizers>
        </y:ProxyAutoBoundsNode>
      </data>
      <graph edgedefault="directed" id=""""+nextSubgraphID+"""">
""")
            nextSubgraphID += 1
        }

        // write child nodes
        cluster.nodes foreach {
            case c: Cluster ⇒
                writeCluster(c, nodeBuffer, edgeBuffer)
            case sen: SourceElementNode ⇒
                writeSourceElementNode(sen, nodeBuffer, edgeBuffer)
        }

        if (isVisibleLevel(cluster.level)) {
            write("""      </graph>
    </node>
""")
        }

        writeEdges(cluster, edgeBuffer)
    }

    private def writeAllChildNodeIdentifiers(node: Node) {
        node.nodes foreach { child ⇒
            if (!child.isCluster) {
                write(child.identifier+"\n")
            }
            else {
                writeAllChildNodeIdentifiers(child)
            }
        }
    }

    override protected def writeSourceElementNode(node: SourceElementNode, nodeBuffer: StringBuffer, edgeBuffer: StringBuffer) {
        // Example:
        //   <node id="n0::n0::n0">
        //     <data key="d5"/>
        //     <data key="d6">
        //       <y:ShapeNode>
        //         <y:Fill color="#FFCC00" transparent="false"/>
        //         <y:BorderStyle color="#000000" type="line" width="1.0"/>
        //         <y:NodeLabel alignment="center" autoSizePolicy="content" borderDistance="0.0" fontFamily="Dialog" fontSize="13" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="19.1328125" modelName="internal" modelPosition="c" textColor="#000000" visible="true" width="114.3349609375" x="5.0" y="5.43359375">java.lang.Integer</y:NodeLabel>
        //         <y:Shape type="rectangle"/>
        //       </y:ShapeNode>
        //     </data>
        //   </node>

        if (configuration.showSourceElementNodes && isVisibleLevel(node.level)) {
            write("""  <node id=""""+node.uniqueID+"""">
    <data key="d5"/>
    <data key="d6">
      <y:ShapeNode>
        <y:Fill color="#FFCC00" transparent="false"/>
        <y:BorderStyle color="#000000" type="line" width="1.0"/>
        <y:NodeLabel alignment="center" autoSizePolicy="content" borderDistance="0.0" fontFamily="Dialog" fontSize="13" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="19.1328125" modelName="internal" modelPosition="c" textColor="#000000" visible="true" width="114.3349609375" x="5.0" y="5.43359375">""")
            write(StringEscapeUtils.escapeXml(node.identifier))
            write("""</y:NodeLabel>
        <y:Shape type="rectangle"/>
      </y:ShapeNode>
    </data>
  </node>
""")
        }

        writeEdges(node, edgeBuffer)
    }

    private def writeEdges(node: Node, edgeBuffer: StringBuffer) {
        // Example:
        //    <edge id="n0::e0" source="n0::n1::n3::n0" target="n0::n0">
        //      <data key="d8"/>
        //      <data key="d9">
        //        <y:PolyLineEdge>
        //          <y:LineStyle color="#000000" type="line" width="1.0"/>
        //          <y:Arrows source="none" target="standard"/>
        //          <y:EdgeLabel alignment="center" distance="2.0" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="17.96875" modelName="side_slider" preferredPlacement="anywhere" ratio="0.0" textColor="#000000" visible="true" width="176.025390625" x="-178.02534985257694" y="10.125">has local variable of type [1]</y:EdgeLabel>
        //          <y:BendStyle smoothed="false"/>
        //        </y:PolyLineEdge>
        //      </data>
        //    </edge>
        // add egdes
        for (e ← node.getOwnEdges) {
            val sourceID = if (configuration.showSourceElementNodes || e.source.isCluster) {
                if (isVisibleLevel(e.source.level))
                    e.source.uniqueID
                else
                    e.source.getNodeOfLevel(configuration.maxNumberOfLevels.get).uniqueID
            }
            else {
                if (isVisibleLevel(e.source.level - 1)) {
                    e.source.parent.uniqueID
                }
                else {
                    e.source.getNodeOfLevel(configuration.maxNumberOfLevels.get).uniqueID
                }
            }
            val targetID = if (configuration.showSourceElementNodes || e.target.isCluster) {
                if (isVisibleLevel(e.target.level))
                    e.target.uniqueID
                else
                    e.target.getNodeOfLevel(configuration.maxNumberOfLevels.get).uniqueID
            }
            else {
                if (isVisibleLevel(e.target.level - 1)) {
                    e.target.parent.uniqueID
                }
                else {
                    e.target.getNodeOfLevel(configuration.maxNumberOfLevels.get).uniqueID
                }
            }
            if (!configuration.aggregateEdges || !aggregatedEdgesSet.contains((sourceID, targetID))) {
                edgeBuffer.append("""    <edge id=""""+{ val id = nextEdgeID; nextEdgeID += 1; id }+"""" source=""""+sourceID+"""" target=""""+targetID+"""">
      <data key="d8"/>
      <data key="d9">
        <y:PolyLineEdge>
          <y:LineStyle color="#000000" type="line" width="1.0"/>
          <y:Arrows source="none" target="standard"/>""")
                if (configuration.showEdgeLabels) {
                    edgeBuffer.append("""          <y:EdgeLabel alignment="center" distance="2.0" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="17.96875" modelName="side_slider" preferredPlacement="anywhere" ratio="0.0" textColor="#000000" visible="true" width="176.025390625" x="-178.02534985257694" y="10.125">""")
                    if (configuration.aggregateEdges) {
                        edgeBuffer.append("aggregated Edge")
                    }
                    else {
                        edgeBuffer.append(StringEscapeUtils.escapeXml(e.dType.toString))
                    }
                    edgeBuffer.append(" ["+e.count+"]</y:EdgeLabel>\n")
                }
                edgeBuffer.append("""          <y:BendStyle smoothed="false"/>
        </y:PolyLineEdge>
      </data>
    </edge>""")
                aggregatedEdgesSet += ((sourceID, targetID))
            }
        }
    }

    private def isVisibleLevel(level: Int): Boolean =
        configuration.maxNumberOfLevels.isEmpty || configuration.maxNumberOfLevels.get >= level
}

trait GraphmlClusteringResultWriterConfiguration {
    val aggregateEdges: Boolean = false
    val showEdgeLabels: Boolean = true
    val showSourceElementNodes: Boolean = true
    val maxNumberOfLevels: Option[Int] = None
}
