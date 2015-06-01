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

    def definition(Closure cl) {
        Definition.runClosure(cl, definitions, this)
        this
    }

    def path(Closure cl) {
        Definition.runClosure(cl, paths, this)
        this
    }
}
