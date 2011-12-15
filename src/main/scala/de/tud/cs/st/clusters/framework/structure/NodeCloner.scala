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

    def createDeepCopy(typeNode: TypeNode): TypeNode = {
        val copy = createCopy(typeNode)
        // TODO: change edge implementation...only IDs should be used here, because copying of nodes
        // makes references unusable...
        //        typeNode.getEdges foreach{ edge =>
        //            copy.addEdge(edge.sou)
        //        }
        copy
    }

    def createDeepCopy(fieldNode: FieldNode): FieldNode = {
        val copy = createCopy(fieldNode)
        copy
    }

    def createDeepCopy(methodNode: MethodNode): MethodNode = {
        val copy = createCopy(methodNode)
        copy
    }

    def createDeepCopy(node: Node): Node = {
        node match {
            case c: Cluster    ⇒ createDeepCopy(c)
            case t: TypeNode   ⇒ createDeepCopy(t)
            case f: FieldNode  ⇒ createDeepCopy(f)
            case m: MethodNode ⇒ createDeepCopy(m)
        }
    }

}