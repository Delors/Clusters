package de.tud.cs.st.clusters.framework.structure

object NodeCloner {
    def createCopy(cluster: Cluster): Cluster =
        new Cluster(cluster.uniqueID, cluster.identifier, cluster.isRootCluster)

    def createCopy(typeNode: TypeNode): TypeNode =
        if (typeNode.clazz.isDefined)
            TypeNode(typeNode.uniqueID, typeNode.identifierFun, typeNode.clazz.get)
        else
            TypeNode(typeNode.uniqueID, typeNode.identifierFun)

    def createCopy(fieldNode: FieldNode): FieldNode =
        if (fieldNode.field.isDefined)
            FieldNode(fieldNode.uniqueID, fieldNode.identifierFun, fieldNode.field.get)
        else
            FieldNode(fieldNode.uniqueID, fieldNode.identifierFun)

    def createCopy(methodNode: MethodNode): MethodNode =
        if (methodNode.method.isDefined)
            MethodNode(methodNode.uniqueID, methodNode.identifierFun, methodNode.method.get)
        else
            MethodNode(methodNode.uniqueID, methodNode.identifierFun)

    def createCopy(node: Node): Node =
        node match {
            case c: Cluster    ⇒ createCopy(c)
            case t: TypeNode   ⇒ createCopy(t)
            case f: FieldNode  ⇒ createCopy(f)
            case m: MethodNode ⇒ createCopy(m)
        }

    def createDeepCopy(
        node: Node,
        edgeFilter: Edge ⇒ Boolean = _ ⇒ false,
        transposedEdgeFilter: Edge ⇒ Boolean = _ ⇒ false): Node = {
        node match {
            case c: Cluster ⇒
                val copy = createCopy(c)
                // add copies of cluster elements to the cluster's copy
                c.getNodes map {
                    createDeepCopy(_)
                } map { node ⇒
                    copy.addNode(node)
                }
                copyEdges(node, copy, edgeFilter, transposedEdgeFilter)
                copy
            case sen: SourceElementNode ⇒
                val copy = createCopy(node)
                copyEdges(node, copy, edgeFilter, transposedEdgeFilter)
                copy
        }
    }

    private def copyEdges(
        node: Node,
        copiedNode: Node,
        edgeFilter: Edge ⇒ Boolean,
        transposedEdgeFilter: Edge ⇒ Boolean) {
        for (edge ← node.getEdges if !edgeFilter(edge)) {
            copiedNode.addEdge(edge.sourceID, edge.targetID, edge.dType)
        }
        for (transposedEdge ← node.getTransposedEdges if !transposedEdgeFilter(transposedEdge)) {
            copiedNode.addEdge(transposedEdge.targetID, transposedEdge.sourceID, transposedEdge.dType)
        }
    }

}