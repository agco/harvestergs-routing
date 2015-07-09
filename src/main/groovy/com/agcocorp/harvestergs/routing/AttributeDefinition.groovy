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

    AttributeDefinition(String type, Closure cl = null, itemType = null) {
        this.type = type
        this.itemsSpec = itemType

        runClosure(cl)
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
        def schema = [type: type]
        schema << propSpec
        schema << getPropsJsonSchema()

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
        return schema;
    }

    static final UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/
}
