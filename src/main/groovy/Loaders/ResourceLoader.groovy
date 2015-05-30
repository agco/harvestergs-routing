package com.agcocorp.harvester.routing

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jackson.JsonLoader
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.fge.jsonschema.main.JsonSchemaFactory
import groovy.json.JsonOutput
import com.fasterxml.jackson.databind.JsonNode

class ResourceLoader {
    def ResourceLoader(specProperties = null,
                       PathVisitor pathVisitor = null,
                       DocumentLoader docLoader = null) {
        this.pathVisitor = new PathVisitor()
        this.docLoader = new DocumentLoader(specProperties)
    }

    def loadResource(Resource spec) {
        loadPath spec
        docLoader.loadDocs spec

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

    private final verbs = ['get', 'patch', 'post', 'delete']
    private pathVisitor
    private docLoader

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
            objectMapper.setSerializationInclusion Include.NON_NULL
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

    private def jsonSchemaFactory = JsonSchemaFactory.byDefault()
    private def objectMapper = new ObjectMapper()

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

