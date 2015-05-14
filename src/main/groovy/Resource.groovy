import groovy.transform.*

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
