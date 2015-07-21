package com.agcocorp.harvestergs.routing

class APIResource {
    final resourceName
    final private attributes = new AttributeSetDefinition()
    final private relationships = new RelationshipSetDefinition()
    final private paths = new PathDefinition()

    APIResource(String name) {
        attributes = new AttributeSetDefinition();
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
                            pattern: AttributeMapper.UUID_PATTERN
                        ]
                    ]
                ]
            ]
        ]
        schema[resourceName].properties.data.properties << attributes.toJsonSchema()
        // todo: refactor this in favor of a more elegant approach. Challenge is to avoid side-effects
        if (attributes && attributes.id) {
            schema[resourceName].properties.data.properties.id = attributes.id.toJsonSchema()
        }

        schema[resourceName].properties.data.properties << relationships.toJsonSchema()
        // todo: Provide ability to override strict schema definition.
        schema[resourceName].properties.data.additionalProperties = false

        return schema

        return attributes.toJsonSchema();
    }

    def getAllPaths() {
        return paths.getAllPaths()
    }
}
