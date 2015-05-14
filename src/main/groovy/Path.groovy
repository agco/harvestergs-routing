import groovy.transform.*

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
