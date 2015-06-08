package com.agcocorp.harvestergs.routing.loaders

import com.agcocorp.harvestergs.routing.Resource
import com.agcocorp.harvestergs.routing.SwaggerLoader
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.fge.jsonschema.main.JsonSchemaFactory
import groovy.json.JsonOutput
import com.fasterxml.jackson.databind.JsonNode

class SparkLoader {
    private final jsonSchemaFactory = JsonSchemaFactory.byDefault()
    private final objectMapper
    private final verbs = ['get', 'patch', 'post', 'delete']
    private final pathVisitor
    private final docLoader

    def SparkLoader(specProperties = null,
                       PathVisitor pathVisitor = null,
                       SwaggerLoader docLoader = null) {
        this.pathVisitor = pathVisitor?: new PathVisitor()
        this.docLoader = docLoader?: new SwaggerLoader(specProperties)
        this.objectMapper = new ObjectMapper()
        this.objectMapper.setSerializationInclusion Include.NON_NULL
    }

    def loadResources(Iterable<Resource> specs) {
        def docs = null
        specs.each {
            loadPath it.paths
            docs = docLoader.loadDocs(it, docs)
            loadValidation it
        }

        docLoader.registerDocs docs

        spark.Spark.exception(ValidationException.class, { e, request, response ->
            response.status(400);
            response.body(JsonOutput.toJson([
                    id: UUID.randomUUID(),
                    title: 'Invalid data',
                    detail: e.validationResults
            ]))

            response.type "application/json"
        });
    }

    private getPogo(req) {
        if (! req.body()) {
            error.invalid('Empty request')
        }
        try {
            req.metaClass.data = objectMapper.readValue(req.body()?: "{}", Map.class)
            return objectMapper.convertValue(req.data, JsonNode.class)
        }
        catch (IOException e) {
            error.invalid('Could not parse JSON message.')
        }
    }

    private validate(req, schema) {
        def pogo = getPogo(req)
        def validationResults = schema.validate pogo, true
        if (!validationResults.isSuccess()) {
            error.invalid(validationResults.messages.toString())
        }
    }

    private validators = [
        'post': { spec, req ->
            def dslSchema = getSchema(spec)
            def postSchema = jsonSchemaFactory.getJsonSchema(objectMapper.valueToTree(dslSchema))

            return validate(req, postSchema)
        },
        'patch': { spec, req ->
            //todo: add validation here -- schema should ignore 'required' and potentially other default rules
            getPogo(req)
        }
    ]

    private def loadPath(Resource spec) {
        def visitor = { path, pathName ->
            verbs.each { verb ->
                if (path[verb]) {
                    def validate = validators[verb]
                    spark.Spark."$verb" pathName, { req, res ->
                        res.type "application/json"
                        if (validate) {
                            validate(spec, req)
                        }

                        res.status defaultCodes[req.requestMethod()]
                        def innerRes = path[verb].run(req, res)
                        def rawJson = JsonOutput.toJson(innerRes)
                        return rawJson
                    }
                }
            }
        }
        pathVisitor.visitPath spec.paths, visitor
    }

    private error = [
            invalid: { results -> throw new ValidationException( validationResults: results ) }
    ]

    private class ValidationException extends RuntimeException {
        String validationResults
    }

    private def getSchema(Resource spec) {
        spec.definitions.schemas[spec.definitions.mainSchemaName]
    }

    private def defaultCodes = [
            GET: 200,
            POST: 201,
            PATCH: 200,
            DELETE: 204
    ]
}

