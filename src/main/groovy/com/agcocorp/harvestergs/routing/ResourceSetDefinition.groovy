package com.agcocorp.harvestergs.routing

class ResourceSetDefinition {
    final resourceList = [:]

    ResourceSetDefinition(Closure cl) {
        cl.delegate = this
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl.call()
    }

    ResourceSetDefinition(Iterable<ResourceDefinition> definitions) {
        definitions.each { resource ->
            resourceList[resource.resourceName] = resource
        }
    }

    def methodMissing(String name, args) {
        def res = new ResourceDefinition(name, args[0])
        resourceList[name] = res
        return res
    }
}