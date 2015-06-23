package com.agcocorp.harvestergs.routing

class RelationshipDefinition {
    final props = [:]

    def propertyMissing(String relationship) {
        // todo: support other kind of 'fk' types besides string
        // todo: consider pattern for 'fk' uuids
        return [ properties: [ type: [enum: [relationship]], id: [ type: 'string' ] ] ]
    }

    def arrayOf(relationship){
        return [ type: 'array', items: relationship ]
    }

    def methodMissing(String name, args) {
        props[name] = args[0]
        this
    }

    def toJsonSchema() {
        if (props) {
            def schema = [relationships: [ properties: [:]]]
            props.each {
                def data = [ properties: [ data: it.value ], additionalProperties: false ]
                schema.relationships.properties[it.key] = data
                schema.relationships.additionalProperties = false
            }
            return schema
        }
        return [:]
    }
}
