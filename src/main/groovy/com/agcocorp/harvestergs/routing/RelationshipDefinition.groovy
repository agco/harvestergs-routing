package com.agcocorp.harvestergs.routing

class RelationshipDefinition extends ItemDefinition {
    private final schemaRef
    private final jsonSchema

    def RelationshipDefinition(String schemaRef) {
        this.schemaRef = schemaRef
        this.jsonSchema = [data: [properties: [type: [enum: [schemaRef]], id: [type: 'string']]/*, additionalProperties: false*/]]
    }

    def RelationshipDefinition(RelationshipDefinition innerRelationship) {
        this.schemaRef = innerRelationship
        this.jsonSchema = [data: [ type: 'array', items: [properties: [type: [enum: [innerRelationship.schemaRef]], id: [type: 'string']]]]]
        //[innerRelationship.schemaRef]], id: [type: 'string']]]
    }

    Map toJsonSchema() {
        ////return [ properties: [ type: [enum: [relationship]], id: [ type: 'string' ] ] ]
        def schema = jsonSchema
        //schema << propSpec
        //schema << getPropsJsonSchema()
    /*
        switch (itemsSpec) {
            case null:
                break;
            case ArrayList:
                // todo: refactor for a more elegant solution -- perhaps some mapping?
                schema.remove('type')
                schema.enum = itemsSpec
                break;
            case AttributeDefinition:
                schema.items = itemsSpec.toJsonSchema()
                schema.additionalProperties = false
                break;
            default:
                schema.items = [ type: itemsSpec ]
                schema.additionalProperties = false
                break;
        }
        */
        return schema;
    }
}
