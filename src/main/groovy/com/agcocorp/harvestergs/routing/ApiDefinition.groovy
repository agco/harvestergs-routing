package com.agcocorp.harvestergs.routing

class ApiDefinition {
    private final resourceList = [:]
    Closure authClosure

    def resources(Closure definition) {
        definition.delegate = this
        definition.resolveStrategy = Closure.DELEGATE_FIRST
        definition.call()
        return this
    }

    def auth(Closure authClosure) {
        this.authClosure = authClosure
        return this
    }

    def addResources(Iterable<ResourceDefinition> resources) {
        resources.each { resource ->
            resourceList[resource.resourceName] = resource
        }
        return this
    }

    def propertyMissing(String name) {
        def res = new ResourceDefinition(name)
        resourceList[name] = res
        return res
    }

    def getAllResources() {
        return resourceList
    }
}
