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

    def createDeepCopy(cluster: Cluster): Cluster = {
        val copy = createCopy(cluster)
        cluster.getNodes map {
            createDeepCopy(_)
        } map { node ⇒
            copy.addNode(node)
        }
        copy
    }

    def createDeepCopy(
        typeNode: TypeNode,
        edgeFilter: Edge ⇒ Boolean,
        transposedEdgeFilter: Edge ⇒ Boolean): TypeNode = {
        val copy = createCopy(typeNode)
        copyEdges(typeNode, copy, edgeFilter, transposedEdgeFilter)
        copy
    }

    def createDeepCopy(
        fieldNode: FieldNode,
        edgeFilter: Edge ⇒ Boolean,
        transposedEdgeFilter: Edge ⇒ Boolean): FieldNode = {
        val copy = createCopy(fieldNode)
        copyEdges(fieldNode, copy, edgeFilter, transposedEdgeFilter)
        copy
    }

    def createDeepCopy(
        methodNode: MethodNode,
        edgeFilter: Edge ⇒ Boolean,
        transposedEdgeFilter: Edge ⇒ Boolean): MethodNode = {
        val copy = createCopy(methodNode)
        copyEdges(methodNode, copy, edgeFilter, transposedEdgeFilter)
        copy
    }

    def createDeepCopy(
        node: Node,
        edgeFilter: Edge ⇒ Boolean = _ ⇒ false,
        transposedEdgeFilter: Edge ⇒ Boolean = _ ⇒ false): Node = {
        node match {
            case c: Cluster    ⇒ createDeepCopy(c)
            case t: TypeNode   ⇒ createDeepCopy(t, edgeFilter, transposedEdgeFilter)
            case f: FieldNode  ⇒ createDeepCopy(f, edgeFilter, transposedEdgeFilter)
            case m: MethodNode ⇒ createDeepCopy(m, edgeFilter, transposedEdgeFilter)
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