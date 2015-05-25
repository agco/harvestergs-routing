import groovy.json.JsonSlurper

class DocumentLoader {
    private slurper = new JsonSlurper()

    def DocumentLoader(PathVisitor pathVisitor = new PathVisitor()) {
        this.pathVisitor = pathVisitor
    }

    private def loadSpec(specName) {
        def spec = getClass().getResourceAsStream('templates/apiSpec.template.json')
        slurper.parseText(spec.text)
    }

    def PathVisitor pathVisitor

    def loadDocs(Resource spec) {
        def root = loadSpec 'api'

        def visitor = { path, pathName ->
            path.properties.each { prop, val ->
                if ((val) && (val.class == VerbSpec)) {
                    println "DocLoader is visiting ${prop} ($val), at path: ${pathName}"
                }
            }
        }

        pathVisitor.visitPath spec.paths, visitor

        spark.Spark.get("/swagger"){ req, res -> root }
    }
}
