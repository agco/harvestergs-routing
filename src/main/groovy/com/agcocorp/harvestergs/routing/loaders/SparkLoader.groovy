package com.agcocorp.harvestergs.routing.loaders

import com.agcocorp.harvestergs.routing.ApiDefinition
import com.agcocorp.harvestergs.routing.ResourceDefinition
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.fge.jsonschema.main.JsonSchemaFactory
import groovy.json.JsonOutput
import com.fasterxml.jackson.databind.JsonNode
import spark.Spark

class SparkLoader {
    private final jsonSchemaFactory = JsonSchemaFactory.byDefault()
    private final objectMapper
    private Closure globalAuth

    def SparkLoader() {
        this.objectMapper = new ObjectMapper()
        this.objectMapper.setSerializationInclusion Include.NON_NULL
        //def sp = new spark.SparkBase()
    }

    def loadApi(ApiDefinition api) {
        if (api.apiProperties['port']) {
            Spark.port((int)api.apiProperties['port'])
        }
        globalAuth = api.authClosure

        api.getAllResources().each {
            loadPath(it.value)
        }
    }

    private getPogo(req, allowNulls = false) {
        if ((!req.body()) && (!allowNulls)) {
            error.invalid('Empty request')
        }
        try {
            req.metaClass.data = objectMapper.readValue(req.body()?: "{}", Map.class)
            return objectMapper.convertValue(req.data, JsonNode.class)
        }
        // the interface is named ignored because we will throw an error of our own
        // with the error.invalid command below.
        catch (IOException ignored) {
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

    private def loadPath(ResourceDefinition spec) {
        //def authHandler = spec.paths? spec.paths.authHandler : null
        def authHandler = globalAuth
        if (authHandler) {
            authHandler.delegate = this
        }

        spec.allPaths.each { path, pathSpec ->
            pathSpec.each { verb, verbSpec ->
                def validate = validators[verb]
                Spark."$verb"(path) { req, res ->
                    res.type "application/vnd.api+json"
                    if ((authHandler) && !(verbSpec.additionalFlags.skipAuth)) {
                        authHandler(req, res)
                    }

                    if (validate) {
                        // todo: refactor here and take hydration outside of validation
                        if ((verbSpec.additionalFlags.skipValidation)) {
                            getPogo(req, true)
                        }
                        else {
                            validate(spec, req)
                        }
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
        invalid: { results ->
            Spark.halt(400, JsonOutput.toJson([
                id: UUID.randomUUID(),
                title: 'Invalid data',
                detail: results.toString()
            ]))
        },
        forbidden: {
            Spark.halt(403)
        },
        unauthorized: {
            Spark.halt(401)
        }
    ]

    private def getSchema(ResourceDefinition spec) {
        spec.toJsonSchema()[spec.resourceName]
    }

    private def defaultCodes = [
        GET: 200,
        POST: 201,
        PATCH: 200,
        DELETE: 204
    ]
}

