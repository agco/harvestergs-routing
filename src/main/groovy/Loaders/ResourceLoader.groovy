package com.agcocorp.harvester.routing

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jackson.JsonLoader
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.fge.jsonschema.main.JsonSchemaFactory
import groovy.json.JsonOutput

class ResourceLoader {
    def ResourceLoader(specProperties = null,
                       PathVisitor pathVisitor = null,
                       DocumentLoader docLoader = null) {
        this.pathVisitor = new PathVisitor()
        this.docLoader = new DocumentLoader(specProperties)
    }

    private final verbs = ['get', 'patch', 'post', 'delete']
    private pathVisitor
    private docLoader

    private getPogo(req) {
        if (! req.body()) {
            error.invalid('Empty request')
        }
        try {
            def pogo = JsonLoader.fromString(req.body()?: "{}")
            return pogo
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

        req.metaClass.data = pogo
    }

    private validators = [
            'post': { spec, req ->
                def dslSchema = getSchema(spec)
                objectMapper.setSerializationInclusion Include.NON_NULL
                def postSchema = jsonSchemaFactory.getJsonSchema(objectMapper.valueToTree(dslSchema))

                return validate(req, postSchema)
            }
    ]

    def loadResource(Resource spec) {
        loadPath spec
        docLoader.loadDocs spec
        //loadValidation spec

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
                        JsonOutput.toJson(innerRes)
                    }
                }
            }
        }
        pathVisitor.visitPath spec.paths, visitor
    }

    private def jsonSchemaFactory = JsonSchemaFactory.byDefault()
    private def objectMapper = new ObjectMapper()


    // todo: extract this method (and exception) so it can be broadly used
    def error = [
            invalid: { results -> throw new ValidationException( validationResults: results ) }
    ]

    class ValidationException extends RuntimeException {
        String validationResults
    }

    private def getSchema(Resource spec) {
        // todo: refactor for a more robust approach to getting the main schema -- should the containing class be an array?
        def dslSchema = spec.definitions.schemas[spec.definitions.mainSchemaName]
    }

    private def defaultCodes = [
            GET: 200,
            POST: 201,
            PATCH: 200,
            DELETE: 204
    ]

    private def verbHandling = [
            GET: [
                    defaultCode: 200,
                    hasBody: false
            ],
            POST: [
                    defaultCode: 201,
                    hasBody: true
            ],
            PATCH: [
                    defaultCode: 200,
                    hasBody: true
            ],
            DELETE: [
                    defaultCode: 204,
                    hasBody: false
            ]
    ]
}

