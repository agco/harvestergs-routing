import groovy.json.JsonSlurper
import groovy.text.SimpleTemplateEngine

class DocumentLoader {
    private slurper = new JsonSlurper()
    private specProperties
    private engine = new SimpleTemplateEngine()
    private templates = [:]

    def DocumentLoader(PathVisitor pathVisitor = new PathVisitor(),
                       specProperties = ['host': 'localhost', 'version': '0.1', 'description': 'api description', 'title': 'api title']) {
        this.pathVisitor = pathVisitor
        this.specProperties = specProperties
    }

    private getTemplate(specName) {
        def specRaw = getClass().getResourceAsStream("templates/${specName}Spec.template.json").text
        if (!templates[specName]) {
            templates[specName] = engine.createTemplate(specRaw)
        }
        templates[specName]
    }

    private def loadSpec(specName, props) {
        def tpl = getTemplate(specName)
        def spec = tpl.make(props).toString()
        slurper.parseText(spec)
    }

    def PathVisitor pathVisitor

    def loadDocs(Resource spec) {
        def root = loadSpec 'api', specProperties

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
