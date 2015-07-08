package com.agcocorp.harvestergs.routing

class PropertyDefinition extends TypeMapper {
    final type

    private void runClosure(Closure cl, delegate = this) {
        if (cl) {
            cl.delegate = delegate
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl.call()
        }
    }

    PropertyDefinition(String type, Closure cl = null, itemType = null) {
        this.type = type
        this.itemsSpec = itemType

        runClosure(cl)
    }

    def itemsSpec
    Map propSpec = [:]
    Map parentSpec = [:]

    PropertyDefinition getRequired() {
        parentSpec['required'] = true
        return this
    }

    PropertyDefinition getReadOnly() {
        propSpec['readOnly'] = true
        return this
    }

    PropertyDefinition pattern(String pattern) {
        propSpec['pattern'] = pattern
        return this
    }

    PropertyDefinition format(String format) {
        propSpec['format'] = format
        return this
    }

    PropertyDefinition description(String description) {
        propSpec['description'] = description
        return this
    }

    def methodMissing(String name, args) {
        props[name] = parseArgs(args)
        return this
    }

    def propertyMissing(String name) {
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
            case PropertyDefinition:
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
}
