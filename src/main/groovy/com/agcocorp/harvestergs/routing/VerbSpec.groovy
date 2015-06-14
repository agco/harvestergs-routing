package com.agcocorp.harvestergs.routing

import groovy.transform.Canonical

@Canonical
class VerbSpec {
    Closure run
    Closure document
    HashSet<String> flags = new HashSet<>()

    VerbSpec(Closure cl) {
        run = cl
    }

    def propertyMissing(String name) {
        flags.add(name)
        // returning 'this' to allow further chaining
        this
    }

    def methodMissing(String name, args) {
        //def verb = new VerbSpec(args[0])
        //Definition.setProperty(this, name, verb)
        Definition.setProperty(this, name, args[0])
        // returning 'this' to allow further chaining
        this
    }

}