package com.agcocorp.harvestergs.routing

class PathDefinition {
    private String root
    final handlers = [:]
    final children = []
    private owner
    private thisObject

    private run(Closure definition, delegate = this) {
        this.owner = definition.owner
        this.thisObject = definition.thisObject
        definition.delegate = delegate
        definition.resolveStrategy = Closure.DELEGATE_FIRST
        definition.call()
        null
    }

    String getRoot() {
        return root
    }

    private registerHandler(String verb, Closure handler) {
        handlers[verb] = new VerbDefinition(handler)
    }

    void authenticate(Closure cl) {

    }

    def get(Closure handler) {
        registerHandler 'get', handler
    }

    def post(Closure handler) {
        registerHandler 'post', handler
    }

    def patch(Closure handler) {
        registerHandler 'patch', handler
    }

    def delete(Closure handler) {
        registerHandler 'delete', handler
    }

    void methodMissing(String name, args) {
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
