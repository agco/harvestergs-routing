package com.agcocorp.harvestergs.routing

class RelationshipSetDefinition {
    final props = [:]

    def propertyMissing(String relationship) {
        // todo: support other kind of 'fk' types besides string
        // todo: consider pattern for 'fk' uuids
        //return [ properties: [ type: [enum: [relationship]], id: [ type: 'string' ] ] ]
        return new RelationshipDefinition(relationship)
    }

    def arrayOf(RelationshipDefinition relationship){
        //return [ type: 'array', items: relationship ]
        return new RelationshipDefinition(relationship)
    }

    def methodMissing(String name, args) {
        props[name] = args[0]
        this
    }

    def toJsonSchema() {
        if (props) {
            def schema = [relationships: [ properties: [:]], additionalProperties: false]
            props.each {
                //def data = [ properties: [ data: it.value ], additionalProperties: false ]
                def data = it.value.toJsonSchema()
                schema.relationships.properties[it.key] = [properties: data]
            }
            return schema
        }
        return [:]
    }
}
