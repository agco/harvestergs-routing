package com.agcocorp.harvestergs.routing
import com.agcocorp.harvestergs.routing.loaders.PathVisitor
import com.agcocorp.harvestergs.routing.loaders.SwaggerSchemaMapper
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import groovy.json.JsonSlurper
import groovy.text.SimpleTemplateEngine

class SwaggerLoader {
    private slurper = new JsonSlurper()
    private specProperties
    private engine = new SimpleTemplateEngine()
    private templates = [:]
    private final defaultProps = ['host': 'localhost', 'version': '0.1.0', 'description': 'api description', 'title': 'api title']
    private final mapSchemaToSwagger
    private final visitPath


    def SwaggerLoader(
        specProperties = null,
        Closure visitPath = new PathVisitor().&visitPath,
        Closure mapSchemaToSwagger = new SwaggerSchemaMapper().&map) {
        this.visitPath = visitPath
        this.specProperties = defaultProps
        this.specProperties << (specProperties?:[:])
        this.mapSchemaToSwagger = mapSchemaToSwagger
    }

    private getTemplate(specName) {
        def specRaw = this.class.getClassLoader().getResourceAsStream(
            "com/agcocorp/harvestergs/routing/${specName}Spec.template.json")
            .text
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

    def camelCase(str) {
        str[0].toLowerCase() + str.substring(1)
    }

    def getPlural(endPoint) {
        if (endPoint) {
            def match = (endPoint =~ ~/\\/([\w-]+)/)
            match[0][1]
        }
        else {
            // todo: try to pluralize first. Also, incorporate a plural override elsewhere in the DSL
            null
        }
    }

    private setIfNotNull(obj, prop, value) {
        if (value) {
            obj[prop] = value
        }
    }

    private convertToSwagger(Schema schema) {
        def swaggerSpec = [:]
        setIfNotNull(swaggerSpec, 'properties', schema.attributes)
    }

    def loadDocs(Resource spec, Map current = null) {
        def root = current?: loadSpec('api', specProperties)
        def resource = spec.definitions.mainSchemaName
        def singular = camelCase(resource)
        def plural = getPlural(spec.paths.rootPathEndpoint)

        def visitor = { path, pathName ->
            path.properties.each { prop, val ->
                if ((val) && (val.class == VerbSpec)) {
                    def verbSpec = loadSpec prop, [
                            'plural'  : plural,
                            'resource': resource,
                            'singular': singular,
                            'ref'     : '$ref']

                    if (! root.paths."$pathName") {
                        root.paths."$pathName" = [:]
                    }

                    root.paths."$pathName"."$prop" = verbSpec
                }
            }
        }

        visitPath spec.paths, visitor
        spec.definitions.schemas.each {
            root.definitions[it.key] = mapSchemaToSwagger(it.value)
        }

        return root
    }

    def registerDocs(docs) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        mapper.setSerializationInclusion(Include.NON_NULL);
        def json = mapper.writeValueAsString(docs);

        spark.Spark.get("/swagger"){ req, res ->
            res.type "application/json"
            json
        }
    }
}
