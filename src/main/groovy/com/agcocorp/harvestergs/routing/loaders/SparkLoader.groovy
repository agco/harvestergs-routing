package com.agcocorp.harvestergs.routing.loaders

import com.agcocorp.harvestergs.routing.APIResource
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.fge.jsonschema.main.JsonSchemaFactory
import groovy.json.JsonOutput
import com.fasterxml.jackson.databind.JsonNode

class SparkLoader {
    private final jsonSchemaFactory = JsonSchemaFactory.byDefault()
    private final objectMapper

    def SparkLoader() {
        this.objectMapper = new ObjectMapper()
        this.objectMapper.setSerializationInclusion Include.NON_NULL
    }

    def loadResources(Iterable<APIResource> specs) {
        specs.each {
            loadPath it
        }
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
            // todo: add validation here -- schema should ignore 'required' and potentially other default rules
            getPogo(req)
        }
    ]

    private def loadPath(APIResource spec) {
        def authHandler = spec.paths? spec.paths.authHandler : null
        if (authHandler) {
            authHandler.delegate = this
        }

        spec.allPaths.each { path, pathSpec ->
            pathSpec.each { verb, verbSpec ->
                def validate = validators[verb]
                // todo: refactor for better composition (eg: use currying to pass the verb as first argument)
                spark.Spark."$verb"(path) { req, res ->
                    res.type "application/json"
                    if (authHandler) {
                        authHandler(req, res)
                    }

                    if (validate) {
                        validate(spec, req)
                    }

                    res.status defaultCodes[req.requestMethod()]
                    def innerRes = verbSpec.handler(req, res)
                    def rawJson = JsonOutput.toJson(innerRes)
                    return rawJson
                }
            }
        }
    }

    private error = [
        //invalid: { results -> throw new ValidationException( validationResults: results ) },
        invalid: { results ->
            spark.Spark.halt(400, JsonOutput.toJson([
                id: UUID.randomUUID(),
                title: 'Invalid data',
                detail: results.toString()
            ]))
        },
        forbidden: {
            spark.Spark.halt(403)
        },
        unauthorized: {
            spark.Spark.halt(401)
        }
    ]

    private def getSchema(APIResource spec) {
        spec.toJsonSchema()[spec.resourceName]
    }

    private def defaultCodes = [
        GET: 200,
        POST: 201,
        PATCH: 200,
        DELETE: 204
    ]
}

