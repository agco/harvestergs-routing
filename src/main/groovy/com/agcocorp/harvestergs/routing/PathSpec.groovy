package com.agcocorp.harvestergs.routing

import groovy.transform.Canonical

@Canonical
class PathSpec {
    private parentPath
    private delegateTo

    def PathSpec(PathSpec parentPath = null, Object delegateTo = null) {
        this.delegateTo = delegateTo
        this.parentPath = parentPath
    }

    Map<String, PathSpec> children = [:]

    VerbSpec get, post, patch, delete

    def methodMissing(String name, args) {
        if (name.startsWith('/')) {
            def innerSpec = new PathSpec(parentPath: this, delegateTo: delegateTo)
            Definition.runClosure(args[0], innerSpec, this)
            children[name] = innerSpec
            return innerSpec
        }

        if (this.hasProperty(name)) {
            def verb = new VerbSpec(args[0])
            Definition.setProperty(this, name, verb)
            return verb
        }

        if ((!delegateTo) || (! delegateTo.hasProperty(name))) {
            throw new MissingMethodException(name, this.class, args)
        }

        return delegateTo."$name"(args)
    }
}
