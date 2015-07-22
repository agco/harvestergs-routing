package com.agcocorp.harvestergs.routing

class AttributeDefinition extends AttributeMapper {
    final type

    protected void runClosure(Closure cl, delegate = this) {
        if (cl) {
            cl.delegate = delegate
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl.call()
        }
    }

    AttributeDefinition(String type) {
        this.type = type
    }

    AttributeDefinition(String type, Closure cl) {
        this.type = type

        runClosure(cl)
    }

    AttributeDefinition(String type, String itemType) {
        this.type = type
        this.itemsSpec = itemType
    }

    AttributeDefinition(String type, AttributeDefinition itemType) {
        this.type = type
        this.itemsSpec = itemType
    }

    AttributeDefinition(ArrayList enumValues) {
        this.type = null
        this.itemsSpec = enumValues
    }

    private def methodMissing(String name, args) {
        props[name] = parseArgs(args)
        return this
    }

    private def propertyMissing(String name) {
        if (! this.type == 'enum') {
            throw new MissingPropertyException()
        }

        if (! itemsSpec) {
            itemsSpec = []
        }
        itemsSpec << name
    }

    Map toJsonSchema() {
        def schema = type? [type: type] : [:]
        schema << propSpec
        schema << getPropsJsonSchema()

        // todo: generate the schema upon construction -- avoids this horrid switch/case
        switch (itemsSpec) {
            case null:
                break;
            case ArrayList:
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
        return schema;
    }

    static final UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/
}
