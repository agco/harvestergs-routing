package com.agcocorp.harvestergs.routing

import groovy.transform.Canonical

@Canonical
class PropertyList extends HashMap<String, Schema> {
    def knownTypes = [
        'string': [ type: 'string' ],
        'integer': [ type: 'integer' ],
        // todo: add uuid pattern here
        'uuid': [ type: 'string' ]
    ]

    def methodMissing(String name, args) {
        def prop = new Schema()
        Definition.runClosure(args[0], prop, this)
        this[name] = prop
    }
}