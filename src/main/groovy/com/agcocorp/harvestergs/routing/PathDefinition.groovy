package com.agcocorp.harvestergs.routing

class PathDefinition {
    private root
    final handlers = [:]
    final children = []

    private run(Closure definition, delegate = this) {
        definition.delegate = delegate
        definition.resolveStrategy = Closure.DELEGATE_ONLY //DELEGATE_FIRST
        definition.call()
        null
    }

    def get(Closure handler) {
        handlers['get'] = new VerbDefinition(handler)
    }

    def post(Closure handler) {
        handlers['post'] = new VerbDefinition(handler)
    }

    def patch(Closure handler) {
        handlers['patch'] = new VerbDefinition(handler)
    }

    def delete(Closure handler) {
        handlers['delete'] = new VerbDefinition(handler)
    }

    def methodMissing(String name, args) {
        if (name.startsWith('/')) {
            if (! root) {
                root = name
                run args[0]
            }
            else {
                def child = new PathDefinition(root: name)
                run args[0], child
                children << child
            }
        }
        else {
            throw new IllegalArgumentException("Paths must start with a '/'. Got $name instead")
        }
    }

    private recursePaths(String path) {
        def allPaths = [:]
        String current = path + root
        allPaths[current] = [:]
        allPaths[current] << handlers
        children.each {
            allPaths << it.recursePaths(current)
        }
        return allPaths
    }

    def getAllPaths() {
        recursePaths ''
    }
}
