package com.agcocorp.harvestergs.routing

import groovy.transform.Canonical

@Canonical
class PropertyList extends HashMap<String, Schema> {
    def methodMissing(String name, args) {
        def prop = new Schema()
        Definition.runClosure(args[0], prop, this)
        this[name] = prop
    }
}