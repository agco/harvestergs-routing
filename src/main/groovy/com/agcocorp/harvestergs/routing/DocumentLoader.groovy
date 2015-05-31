package com.agcocorp.harvestergs.routing

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.text.SimpleTemplateEngine

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jackson.JsonLoader
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.fge.jsonschema.main.JsonSchemaFactory

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;

class DocumentLoader {
    private slurper = new JsonSlurper()
    private specProperties
    private engine = new SimpleTemplateEngine()
    private templates = [:]
    private final defaultProps = ['host': 'localhost', 'version': '0.1.0', 'description': 'api description', 'title': 'api title']

    def DocumentLoader(specProperties = null,
                       PathVisitor pathVisitor = new PathVisitor()) {
        this.pathVisitor = pathVisitor
        this.specProperties = defaultProps
        this.specProperties << (specProperties?:[:])
    }

    private getTemplate(specName) {
        def specRaw = this.class.getClassLoader().getResourceAsStream("templates/${specName}Spec.template.json").text
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

    def camelCase(str) {
        str[0].toLowerCase() + str.substring(1)
    }

    def getPlural(endPoint) {
        def match = (endPoint =~ ~/\\/([\w-]+)/)
        match[0][1]
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

        root.definitions = spec.definitions.schemas

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
