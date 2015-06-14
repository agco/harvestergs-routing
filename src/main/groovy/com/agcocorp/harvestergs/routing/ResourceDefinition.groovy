package com.agcocorp.harvestergs.routing

//todo: rename this to 'Resource' as the last refactoring step
class ResourceDefinition {
    final private resourceName
    final private attributes = new AttributeDefinition()
    final private relationships = new RelationshipDefinition()
    final private paths

    ResourceDefinition(String name) {
        attributes = new AttributeDefinition();
        this.resourceName = name
    }

    private run(Closure cl, Object delegate) {
        cl.delegate = delegate
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl.call()
    }

    def relationships(Closure cl) {
        run cl, relationships
        this
    }

    def attributes(Closure cl) {
        run cl, attributes
        this
    }

    def propertyMissing(String name, args) {
        println "method missing: $name"
        this
    }

    def paths(Closure cl) {
        this
    }

    def toJsonSchema() {
        def schema = [:]
        schema[resourceName] = [properties: [data: [properties: [:]]]]
        schema[resourceName].properties.data.properties << attributes.toJsonSchema()
        schema[resourceName].properties.data.properties << relationships.toJsonSchema()
        return schema

        return attributes.toJsonSchema();
    }
}
