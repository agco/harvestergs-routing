package com.agcocorp.harvestergs.routing

class ApiDefinition {
    private ResourceSetDefinition allResources
    private final resourceList = [:]
    final apiProperties = [:]
    Closure authClosure

    def ApiDefinition() {}

    def ApiDefinition(Closure cl) {
        cl.delegate = this
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl.call()
    }

    def resources(Closure definition) {
        definition.delegate = this
        definition.resolveStrategy = Closure.DELEGATE_FIRST
        definition.call()
        return this
    }

    def res(Closure cl) {
        allResources = new ResourceSetDefinition(cl)
        return this
    }

    def auth(Closure authClosure) {
        this.authClosure = authClosure
        return this
    }

    private def setProp(name, value) {
        apiProperties[name] = value
        return this
    }

    def port(Integer portNumber) { setProp('port', portNumber) }

    def host(String value) { setProp('host', value) }

    def version(String value) { setProp('version', value) }

    def description(String value) { setProp('description', value) }

    def title(String value) { setProp('title', value) }

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
        // todo: remove resourceList usage at the end of the refactoring
        if (resourceList)
            return resourceList

        return allResources? allResources.resourceList : null
    }
}
