// ----------------------------------------------
// Builder implementation and supporting classes.
// ----------------------------------------------
import com.oracle.webservices.internal.api.message.ContentType
import groovy.transform.*

@Canonical
class Definition {
    Map<String, Schema> schemas = [:]

    def methodMissing(String name, args) {
        println "Definition.MethodMissing: $name"
        def schema = new Schema()
        schemas[name] = schema
        Definition.runClosure(args[0], schema, this)
        schemas
    }

    static def runClosure(Closure cl, Object delegate, Object owner) {
        println "Definition.runClosure"
        def code = cl.rehydrate(delegate, owner, owner)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
    }

    static def setProperty(Object obj, String property, Object value, boolean throwOnMiss = true) {
        if (obj.hasProperty(property)) {
            obj[property] = value
            return true
        }
        if (throwOnMiss) {
            throw new MissingPropertyException(property, obj.class)
        }
    }
}

@Canonical
class Path {
    Map<String, PathSpec> paths = [:]

    def methodMissing(String name, args) {
        println "Path.MethodMissing: $name"
        def spec = new PathSpec()
        paths[name] = spec
        Definition.runClosure(args[0], spec, this)
        paths
    }
}

@Canonical
class PathSpec {
    private PathSpec parent

    PathSpec(parent = null) {
        this.parent = parent
    }

    Map<String, PathSpec> children = [:]

    VerbSpec get, post, patch, delete

    def methodMissing(String name, args) {
        println "PathSpec.MethodMissing: $name"
        if (name.startsWith('/')) {
            def innerSpec = new PathSpec(this)
            Definition.runClosure(args[0], innerSpec, this)
            children[name] = innerSpec
            return innerSpec
        }
        def verb = new VerbSpec(args[0])
        Definition.setProperty(this, name, verb)
        verb
    }
}

@Canonical
class VerbSpec {
    Closure run
    VerbSpec document
    HashSet<String> flags = new HashSet<>()

    VerbSpec(Closure cl) {
        run = cl
    }

    def propertyMissing(String name) {
        println "VerbSpec.propertyMissing $name"
        flags.add(name)
        // returning 'this' to allow further chaining
        this
    }

    def methodMissing(String name, args) {
        println "VerbSpec.MethodMissing: $name"
        def verb = new VerbSpec(args[0])
        Definition.setProperty(this, name, verb)
        // returning 'this' to allow further chaining
        this
    }

}


@Canonical
class Schema {
    PropertyList properties = new PropertyList()

    List<String> required = []

    def methodMissing(String name, args) {
        println "Schema.methodMissing $name"
        switch (name) {
            case "properties":
                Definition.runClosure(args[0], properties, this);
                break;
            case "required":
                this.required = args
        }
    }
}

@Canonical
class PropertyList extends HashMap<String, Property> {
    def methodMissing(String name, args) {
        println "PropertyList.methodMissing $name"
        def prop = new Property()
        Definition.runClosure(args[0], prop, this)
        this[name] = prop
    }
}

@Canonical
class Property {
    String type
    String description

    def methodMissing(String name, args) {
        println "Property.methodMissing $name"
        Definition.setProperty(this, name, args[0])
    }
}

@Canonical
class Resource {
    Definition definition = new Definition()
    Path path = new Path()

    def definition(Closure cl) {
        Definition.runClosure(cl, definition, this)
        this
    }

    def path(Closure cl) {
        Definition.runClosure(cl, path, this)
        this
    }
}

def d = new Definition().Comment {
    properties {
        id {
            type 'Integer'
            description 'The comment id'
        }
        name {
            type 'String'
            description 'The comment name'
        }
    }
    required 'id', 'name'
}


assert d.Comment.properties.size() == 2
assert d.Comment.properties.id.type == 'Integer'

println d

def p = new Path()."/comments" {
    get { req, res ->
        "comments.GET"
    }

    post { req, res ->
        "comments.POST"
    }.document { docs ->
        docs.description = "Description for comments.POST"
        docs
    }
    .skipAuth
    .skipValidation

    "/:id" {
        get    {req, res -> "comments/:id.GET"}
        patch  {req, res -> "comments/:id.PATCH"}
            .document { docs -> docs.operationId = "commentUpdate"; docs }
        delete {req, res -> "comments/:id.DELETE"}
    }
}

assert p."/comments"
assert p."/comments".get.run(null, null) == "comments.GET"
assert p."/comments".post.run(null, null) == "comments.POST"
assert p."/comments".post.document.run([ summary: "Summary for comments.POST"]) ==
        [ summary: "Summary for comments.POST", description: "Description for comments.POST"]

assert p."/comments".children."/:id".get.run(null, null) == "comments/:id.GET"
assert p."/comments".children."/:id".patch.document.run([:]) == [ operationId: "commentUpdate" ]

assert p."/comments".post.flags.contains('skipAuth')
assert p."/comments".post.flags.contains('skipValidation')

println p

def spec = new Resource()
def s = new Resource()
    .definition {
        Comment {
            properties {
                id {
                    type 'Integer'
                    description 'The comment id'
                }
                name {
                    type 'String'
                    description 'The comment name'
                }
            }
            required 'id', 'name'
        }
                .path
    }
    .path {
        "/comments" {
            get { req, res ->
                "comments.get"
            }

            post { req, res ->
                "comments.post"
            }.document { docs ->
                docs.description = "Description for comments.POST"
                docs
            }
            .skipAuth
                    .skipValidation

            "/:id" {
                get    {req, res -> "comments/1.get"}
                patch  {req, res -> "comments/1.patch"}
                        .document { docs -> docs.operationId = "commentUpdate"; docs }
                delete {req, res -> "comments/1.delete"}
            }
        }
    }

assert s.definition
assert s.path

assert s.path.paths["/comments"]
assert s.path.paths["/comments"].get.run(null, null) == "comments.get"
assert s.path.paths["/comments"].post.run(null, null) == "comments.post"
assert s.path.paths["/comments"].post.document.run([ summary: "Summary for comments.POST"]) ==
        [ summary: "Summary for comments.POST", description: "Description for comments.POST"]

assert s.path.paths["/comments"].children."/:id".get.run(null, null) == "comments/1.get"
assert s.path.paths["/comments"].children."/:id".patch.document.run([:]) == [ operationId: "commentUpdate" ]

assert s.path.paths["/comments"].post.flags.contains('skipAuth')
assert s.path.paths["/comments"].post.flags.contains('skipValidation')

assert s.definition.schemas.Comment.properties.size() == 2
assert s.definition.schemas.Comment.properties.size() == 2
assert s.definition.schemas.Comment.properties.id.type == 'Integer'

println s

@Grab('com.sparkjava:spark-core:2.1')
class RouteLoader {
    def verbs = ['get', 'patch', 'post', 'delete']
    def loadResource(Resource spec) {
        loadPath(spec.path)
    }

    private def loadPath(Path pathSet) {
        pathSet.paths.each { path ->
            loadPathSpec(path.value, path.key)
        }
    }

    private def loadPathSpec(PathSpec path, String pathName) {
        println "registering path $pathName"
        verbs.each { verb ->
            if (path[verb]) {
                println "registering verb ${pathName}.${verb}"
                spark.Spark."$verb"(pathName, path[verb].run)
            }
        }
        path.children.each {
            loadPathSpec(it.value, pathName + it.key)
        }
    }
}

new RouteLoader().loadResource(s)

@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7.1')

import groovyx.net.http.RESTClient
import groovyx.net.http.ContentType
import static groovyx.net.http.ContentType.JSON


def client = new RESTClient('http://localhost:4567')
def targets = [
        "comments" : [ "get", "post" ],
        "comments/1": ["get", "patch", "delete"]
]
targets.each { path ->
    path.value.each { verb ->
        def res = client."$verb"(path: path.key, requestContentType: ContentType.JSON)
        assert res.status == 200
        assert res.responseData == "${path.key}.${verb}"
    }
}

spark.Spark.stop()
