package com.agcocorp.harvestergs.routing

class RelationshipSetDefinition {
    final props = [:]

    def propertyMissing(String relationship) {
        // todo: support other kind of 'fk' types besides string
        // todo: consider pattern for 'fk' uuids
        return new RelationshipDefinition(relationship)
    }

    def arrayOf(RelationshipDefinition relationship){
        return new RelationshipDefinition(relationship)
    }

    def methodMissing(String name, args) {
        props[name] = args[0]
        this
    }

    def toJsonSchema() {
        if (props) {
            def schema = [relationships: [properties: [:], additionalProperties: false]]
            props.each {
                def data = it.value.toJsonSchema()
                schema.relationships.properties[it.key] = data
            }
            return schema
        }
        return [:]
    }
}
