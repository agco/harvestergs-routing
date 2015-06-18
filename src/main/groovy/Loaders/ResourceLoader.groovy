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
            response.body(e.validationResults)

            response.type "application/json"
            response.body()
        });

    }

    private def loadPath(Path pathSet) {
        def visitor = { path, pathName ->
            verbs.each { verb ->
                if (path[verb]) {
                    spark.Spark."$verb" pathName, path[verb].run
                }
            }
        }
        pathVisitor.visitPath pathSet, visitor
    }

    private def loadDocs(Resource spec) {
        def swaggerDoc = "foobar"
        spark.Spark.get("/swagger"){ req, res -> swaggerDoc }
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
            DELETE: 204,
            OPTIONS: 200
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
            ],
            OPTIONS:[
                    defaultCode: 200,
                    hasBody: false
            ]
    ]

    private def loadValidation(Resource spec) {
        def dslSchema = getSchema(spec)
        objectMapper.setSerializationInclusion Include.NON_NULL
        def postSchema = jsonSchemaFactory.getJsonSchema(objectMapper.valueToTree(dslSchema))
        def visitor = { path, pathName ->
            spark.Spark.before(pathName){ req, res ->

                if (req.requestMethod() == 'POST') {
                    def pogo = JsonLoader.fromString(req.body()?: "{}")
                    // todo: handle parsing errors -- shouldn't they all return a 400?
                    def validationResults = postSchema.validate pogo, true
                    if (!validationResults.isSuccess()) {
                        def errors = convertErrors(validationResults.messages)
                        error.invalid JsonOutput.toJson(errors)
                    }
                }
            }

            spark.Spark.after(pathName){ req, res ->
                res.status defaultCodes[req.requestMethod()]
                //res.type "application/json"
                //res.body JsonOutput.toJson(res.body())
            }
        }

        pathVisitor.visitPath spec.paths, visitor
    }

    private def convertErrors(messages){
        def obj =[errors:[]];

        for(item in messages)
        {
            obj.errors.add([
                    id:UUID.randomUUID(),
                    status:400,
                    title: 'Invalid data',
                    detail: item.map.message._value,
                    paths:[
                            level: item.map.level._value,
                            schema:item.map.schema._children.pointer._value,
                            instance:item.map.instance._children.pointer._value,
                            domain:item.map.domain._value,
                            keyword:item.map.keyword._value,
                            message:item.map.message._value,
                            found:item.map.found?item.map.found._value:null,
                            expected:item.map.expected?item.map.expected._children.toString():null
                    ]
            ])
        }
        obj
    }

    private def recursePath(Path pathSet, Closure visitor) {
        pathSet.paths.each { path ->
            recursePath path.value, path.key, visitor
        }
    }

    private def recursePath(PathSpec path, String pathName, Closure visitor) {
        visitor(path, pathName)

        path.children.each {
            recursePath it.value, pathName + it.key, visitor
        }
    }

}

