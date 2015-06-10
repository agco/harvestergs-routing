package com.agcocorp.harvestergs.routing

import groovy.transform.Canonical

@Canonical
class RelationshipList extends HashMap<String, Relationship> {
    def methodMissing(String name, args) {
        def prop = new Relationship()
        Definition.runClosure(args[0], prop, this)
        this[name] = prop
    }
}