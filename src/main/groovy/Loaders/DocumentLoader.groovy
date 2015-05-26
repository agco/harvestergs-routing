import groovy.json.JsonSlurper
import groovy.text.SimpleTemplateEngine

class DocumentLoader {
    private slurper = new JsonSlurper()
    private specProperties
    private engine = new SimpleTemplateEngine()
    private templates = [:]

    def DocumentLoader(specProperties = null,
                       PathVisitor pathVisitor = new PathVisitor()) {
        this.pathVisitor = pathVisitor
        this.specProperties = specProperties?:
                ['host': 'localhost', 'version': '0.1', 'description': 'api description', 'title': 'api title']
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
                    //println "DocLoader is visiting ${prop} ($val), at path: ${pathName}"
                    def verbSpec = loadSpec prop, [
                            'plural'  : 'plural',
                            'resource': 'resource',
                            'singular': 'singular',
                            'ref'     : '$ref']

                    println verbSpec
                }
            }
        }

        pathVisitor.visitPath spec.paths, visitor

        spark.Spark.get("/swagger"){ req, res -> root }
    }
}
