import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.main.JsonSchemaFactory
import groovy.json.JsonOutput

//import static java.util.UUID.randomUUID

class ResourceLoader {
    private final verbs = ['get', 'patch', 'post', 'delete']

    def loadResource(Resource spec) {
        loadPath spec.paths
        loadDocs spec
        loadValidation spec

        spark.Spark.exception(ValidationException.class, { e, request, response ->
            response.status(400);
            response.body(JsonOutput.toJson([
                    id: UUID.randomUUID(),
                    title: 'Invalid data',
                    detail: e.validationResults
            ]));
            response.type("application/json");
        });

    }

    private def loadPath(Path pathSet) {
        pathSet.paths.each { path ->
            loadPathSpec path.value, path.key
        }
    }

    private def loadPathSpec(PathSpec path, String pathName) {
        println "registering path $pathName"
        verbs.each { verb ->
            if (path[verb]) {
                println "registering verb ${pathName}.${verb}"
                spark.Spark."$verb" pathName, path[verb].run
            }
        }
        path.children.each {
            loadPathSpec it.value, pathName + it.key
        }
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
        def dslSchema = spec.definitions.schemas.iterator().next().value
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

    private def loadValidation(Resource spec) {
        def dslSchema = getSchema(spec)
        def postSchema = jsonSchemaFactory.getJsonSchema(objectMapper.valueToTree(dslSchema))
        recursePath (spec.paths, { path, pathName ->
            spark.Spark.before(pathName){ req, res ->
                if (req.requestMethod() == 'POST') {
                    def pogo = JsonLoader.fromString(req.body()?:"{}")
                    // todo: handle parsing errors -- shouldn't they all return a 400?
                    def validationResults = postSchema.validate(pogo)
                    if (!validationResults.isSuccess()) {
                        error.invalid validationResults.messages.toString()
                    }
                }
            }

            spark.Spark.after(pathName){ req, res ->
                res.status defaultCodes[req.requestMethod()]
            }
        })
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

