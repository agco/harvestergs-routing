// ----------------------------------------------
// Builder implementation and supporting classes.
// ----------------------------------------------
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
    PathSpec parent

    VerbSpec get, post, patch, delete

    def methodMissing(String name, args) {
        println "PathSpec.MethodMissing: $name"
        def verb = new VerbSpec(args[0])
        Definition.setProperty(this, name, verb)
        verb
    }
}

@Canonical
class VerbSpec {
    Closure run

    VerbSpec document

    VerbSpec(Closure cl) {
        run = cl
    }

    def methodMissing(String name, args) {
        println "VerbSpec.MethodMissing: $name"

        Definition.setProperty(this, name, new VerbSpec(args[0]))
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
}

assert p."/comments"
assert p."/comments".get.run(null, null) == "comments.GET"
assert p."/comments".post.run(null, null) == "comments.POST"
assert p."/comments".post.document.run([ summary: "Summary for comments.POST"]) ==
        [ summary: "Summary for comments.POST", description: "Description for comments.POST"]

println p

