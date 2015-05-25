class DocumentLoader {
    def DocumentLoader(PathVisitor pathVisitor = new PathVisitor()) {
        this.pathVisitor = pathVisitor
    }

    def PathVisitor pathVisitor

    def loadDocs(Resource spec) {
        def visitor = { path, pathName ->
            path.each { println it }
        }
    }
}
