package com.agcocorp.harvestergs.routing.loaders

import com.agcocorp.harvestergs.routing.Resource
import com.agcocorp.harvestergs.routing.Schema
import com.agcocorp.harvestergs.routing.VerbSpec
import groovy.json.JsonSlurper
import groovy.text.SimpleTemplateEngine
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

class SwaggerLoader {
    private slurper = new JsonSlurper()
    private specProperties
    private engine = new SimpleTemplateEngine()
    private templates = [:]
    private final defaultProps = ['host': 'localhost', 'version': '0.1.0', 'description': 'api description', 'title': 'api title']
    private final mapSchemaToSwagger

    def SwaggerLoader(
        specProperties = null,
        //todo: turn this into a closure
        PathVisitor pathVisitor = new PathVisitor(),
        Closure mapSchemaToSwagger = new SwaggerSchemaMapper().&map) {
        this.pathVisitor = pathVisitor
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

    final PathVisitor pathVisitor

    def camelCase(str) {
        str[0].toLowerCase() + str.substring(1)
    }

    def getPlural(endPoint) {
        def match = (endPoint =~ ~/\\/([\w-]+)/)
        match[0][1]
    }

    private final uuidPattern = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/

    private setIfNotNull(obj, prop, value) {
        if (value) {
            obj[prop] = value
        }
    }

    private convertToSwagger(Schema schema) {
        def swaggerSpec = [:]
        setIfNotNull(swaggerSpec, 'properties', schema.attributes)
    }

    def loadDocs(Resource spec) {
        def root = loadSpec 'api', specProperties
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

        pathVisitor.visitPath spec.paths, visitor
        spec.definitions.schemas.each {
            // todo: create proper tests to validate the id UUID pattern
            /*
            root.definitions[it.key] = loadSpec('definition', [
                'plural': plural,
                'idType': 'string',
                'idPattern': uuidPattern ])
            root.definitions[it.key].properties.data.properties.attributes.properties = it.value.attributes
            */
            root.definitions[it.key] = mapSchemaToSwagger(it.value)
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        mapper.setSerializationInclusion(Include.NON_NULL);
        def json = mapper.writeValueAsString(root);

        spark.Spark.get("/swagger"){ req, res ->
            res.type "application/json"
            json
        }
    }
}
