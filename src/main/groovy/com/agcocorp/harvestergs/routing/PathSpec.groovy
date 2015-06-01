package com.agcocorp.harvestergs.routing

import groovy.transform.Canonical

@Canonical
class PathSpec {
    private parentPath
    private builder

    def PathSpec(PathSpec parentPath = null, Object builder = null) {
        this.builder = builder
        this.parentPath = parentPath
    }

    Map<String, PathSpec> children = [:]

    VerbSpec get, post, patch, delete

    def methodMissing(String name, args) {
        if (name.startsWith('/')) {
            def innerSpec = new PathSpec(parentPath: this, builder: builder)
            Definition.runClosure(args[0], innerSpec, this)
            children[name] = innerSpec
            return innerSpec
        }

        if (this.hasProperty(name)) {
            def verb = new VerbSpec(args[0])
            Definition.setProperty(this, name, verb)
            return verb
        }

        return builder."$name"(args)
    }
}
