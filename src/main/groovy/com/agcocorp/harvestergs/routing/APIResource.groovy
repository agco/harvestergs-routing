package com.agcocorp.harvestergs.routing

class APIResource {
    final resourceName
    final private attributes = new AttributeDefinition()
    final private relationships = new RelationshipDefinition()
    final private paths = new PathDefinition()

    APIResource(String name) {
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

    def paths(Closure cl) {
        run cl, paths
        this
    }

    def toJsonSchema() {
        def schema = [:]
        schema[resourceName] = [
            properties: [
                data: [
                    properties: [
                        type: [enum: [resourceName]],
                        id: [
                            type: 'string',
                            pattern: TypeMapper.UUID_PATTERN
                        ]
                    ]
                ]
            ]
        ]
        schema[resourceName].properties.data.properties << attributes.toJsonSchema()
        schema[resourceName].properties.data.properties << relationships.toJsonSchema()
        return schema

        return attributes.toJsonSchema();
    }

    def getAllPaths() {
        return paths.getAllPaths()
    }
}
