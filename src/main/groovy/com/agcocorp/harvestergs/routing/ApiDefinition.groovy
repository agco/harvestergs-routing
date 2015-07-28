package com.agcocorp.harvestergs.routing

class ApiDefinition {
    private ResourceSetDefinition allResources
    final apiProperties = [:]
    Closure authClosure

    def ApiDefinition() {}

    def ApiDefinition(Closure cl) {
        cl.delegate = this
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl.call()
    }

    def apiResources(Closure cl) {
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
        allResources = new ResourceSetDefinition(resources)
        return this
    }

    def getAllResources() {
        return allResources? allResources.resourceList : null
    }
}
