package com.agcocorp.harvester.routing

import groovy.transform.Canonical

@Canonical
class Resource {
    Definition definitions = new Definition()
    Path paths = new Path()

    def definitions(Closure cl) {
        Definition.runClosure(cl, definitions, this)
        this.definitions
    }

    def paths(Closure cl) {
        Definition.runClosure(cl, paths, this)
        this
    }
}
