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
        filter: Edge ⇒ Boolean,
        filterTransposed: Edge ⇒ Boolean): TypeNode = {
        val copy = createCopy(typeNode)
        copyEdges(typeNode, copy, filter, filterTransposed)
        copy
    }

    def createDeepCopy(
        fieldNode: FieldNode,
        filter: Edge ⇒ Boolean,
        filterTransposed: Edge ⇒ Boolean): FieldNode = {
        val copy = createCopy(fieldNode)
        copyEdges(fieldNode, copy, filter, filterTransposed)
        copy
    }

    def createDeepCopy(
        methodNode: MethodNode,
        filter: Edge ⇒ Boolean,
        filterTransposed: Edge ⇒ Boolean): MethodNode = {
        val copy = createCopy(methodNode)
        copyEdges(methodNode, copy, filter, filterTransposed)
        copy
    }

    def createDeepCopy(
        node: Node,
        filter: Edge ⇒ Boolean = _ ⇒ false,
        filterTransposed: Edge ⇒ Boolean = _ ⇒ false): Node = {
        node match {
            case c: Cluster    ⇒ createDeepCopy(c)
            case t: TypeNode   ⇒ createDeepCopy(t, filter, filterTransposed)
            case f: FieldNode  ⇒ createDeepCopy(f, filter, filterTransposed)
            case m: MethodNode ⇒ createDeepCopy(m, filter, filterTransposed)
        }
    }

    private def copyEdges(
        node: Node,
        copiedNode: Node,
        filter: Edge ⇒ Boolean,
        filterTransposed: Edge ⇒ Boolean) {
        for (edge ← node.getEdges if !filter(edge)) {
            copiedNode.addEdge(edge.sourceID, edge.targetID, edge.dType)
        }
        for (transposedEdge ← node.getTransposedEdges if !filterTransposed(transposedEdge)) {
            copiedNode.addEdge(transposedEdge.targetID, transposedEdge.sourceID, transposedEdge.dType)
        }
    }

}