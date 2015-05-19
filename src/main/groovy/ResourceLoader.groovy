class ResourceLoader {
    private final verbs = ['get', 'patch', 'post', 'delete']

    def loadResource(Resource spec) {
        loadPath spec.paths
        loadDocs spec
        loadValidation spec
    }

    private def loadPath(Path pathSet) {
        pathSet.paths.each { path ->
            loadPathSpec path.value, path.key
        }
    }

    private def loadPathSpec(PathSpec path, String pathName) {
        println "registering path $pathName"
        verbs.each { verb ->
            if (path[verb]) {
                println "registering verb ${pathName}.${verb}"
                spark.Spark."$verb" pathName, path[verb].run
            }
        }
        path.children.each {
            loadPathSpec it.value, pathName + it.key
        }
    }

    private def loadDocs(Resource spec) {
        def swaggerDoc = "foobar"
        spark.Spark.get("/swagger"){ req, res -> swagerDoc }
    }

    private def loadValidation(Resource spec) {
        spec.paths.paths.each {
            spark.Spark.before("${it.key}"){ req, res ->
                if (req.requestMethod() == 'POST'){
                    println "request contents: ${req.body()}"
                    if (!req.body()?.contains('name')) {
                        spark.Spark.halt(400)
                    }
                }
            }
        }
    }
}

