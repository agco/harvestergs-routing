package com.agcocorp.harvestergs.routing

import groovy.transform.Canonical

@Canonical
class Resource {
    Definition definitions = new Definition()
    Path paths = new Path()

    def definition(Closure cl) {
        Definition.runClosure(cl, definitions, this)
        this
    }

    def path(Closure cl) {
        Definition.runClosure(cl, path, this)
        this
    }
}
