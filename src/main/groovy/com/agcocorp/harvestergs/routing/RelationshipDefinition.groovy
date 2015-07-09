package com.agcocorp.harvestergs.routing

class RelationshipDefinition extends ItemDefinition {
    private final schemaRef
    private final jsonSchema

    def RelationshipDefinition(String schemaRef) {
        this.schemaRef = schemaRef
        //todo: make schema strict within data element as well.
        this.jsonSchema = [properties: [data: [properties: [type: [enum: [schemaRef]], id: [type: 'string']], additionalProperties: false]], additionalProperties: false]
    }

    def RelationshipDefinition(RelationshipDefinition innerRelationship) {
        this.schemaRef = innerRelationship
        this.jsonSchema = [properties: [data: [ type: 'array', items: [properties: [type: [enum: [innerRelationship.schemaRef]], id: [type: 'string']]]]], additionalProperties: false]
    }

    Map toJsonSchema() {
        return jsonSchema << propSpec
        //schema << propSpec
        //schema << getPropsJsonSchema()
    }
}
