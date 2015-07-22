package com.agcocorp.harvestergs.routing.loaders

import com.agcocorp.harvestergs.routing.APIResource
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import groovy.json.JsonSlurper
import groovy.text.SimpleTemplateEngine
import spark.Spark

class SwaggerLoader {
    private slurper = new JsonSlurper()
    private specProperties
    private engine = new SimpleTemplateEngine()
    private templates = [:]
    private final defaultProps = ['host': 'localhost', 'version': '0.1.0', 'description': 'api description', 'title': 'api title']

    def SwaggerLoader(
        specProperties = null) {
        this.specProperties = defaultProps
        this.specProperties << (specProperties?:[:])
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

    private def loadSpecTemplate(specName, props) {
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

    private loadSpec(APIResource spec, Map current = null) {
        def root = current?: loadSpecTemplate('api', specProperties)
        def resource = spec.resourceName
        def singular = camelCase(resource)
        def plural = getPlural(spec.paths.root)

        def schema = spec.toJsonSchema()
        root.definitions << schema

        spec.allPaths.each { path, pathSpec ->
            def currentPath = [:]
            pathSpec.each { verb, verbSpec ->
                def tplName = verb == 'get' && path.endsWith('/:id')?
                    'getById' : verb
                def verbTpl = loadSpecTemplate tplName, [
                    'plural'  : plural,
                    'resource': resource,
                    'singular': singular,
                    'ref'     : '$ref']

                if (verbSpec.document) {
                    verbTpl = verbSpec.document.call(verbTpl)
                }

                currentPath[verb] = verbTpl
            }
            root.paths[path] = currentPath
        }

        return root
    }

    private registerDocs(docs) {
        ObjectMapper mapper = new ObjectMapper()
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
        mapper.setSerializationInclusion(Include.NON_NULL)
        def json = mapper.writeValueAsString(docs)

        Spark.get("/swagger"){ req, res ->
            res.type "application/vnd.api+json"
            json
        }
    }

    def loadDocs(Iterable<APIResource> specs) {
        def docs = null
        specs.each {
            docs = this.loadSpec it, docs
        }

        registerDocs docs
    }
}
