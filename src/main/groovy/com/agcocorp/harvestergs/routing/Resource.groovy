package com.agcocorp.harvestergs.routing

import groovy.transform.Canonical

@Canonical
class Resource {
    Definition definitions
    Path paths

    def Resource(builder) {
        paths = new Path(builder)
        definitions = new Definition()
    }

    def definition(Closure cl) {
        Definition.runClosure(cl, definitions, this)
        this
    }

    def path(Closure cl) {
        Definition.runClosure(cl, paths, this)
        this
    }
}
