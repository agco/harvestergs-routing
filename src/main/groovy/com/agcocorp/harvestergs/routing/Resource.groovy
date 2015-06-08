package com.agcocorp.harvestergs.routing

import groovy.transform.Canonical

@Canonical
class Resource {
    Definition definitions
    Path paths

    def Resource(delegateTo) {
        paths = new Path(delegateTo)
        definitions = new Definition()
    }

    def definitions(Closure cl) {
        Definition.runClosure(cl, definitions, this)
        this
    }

    def paths(Closure cl) {
        Definition.runClosure(cl, paths, this)
        this
    }
}
