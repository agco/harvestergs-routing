package com.agcocorp.harvestergs.routing

class PropertyDefinition {
    final type
    @Delegate
    final TypeMapper mapper = new TypeMapper()

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

    PropertyDefinition description(String description) {
        propSpec['description'] = description
        return this
    }

    def methodMissing(String name, args) {
        props[name] = parseArgs(args)
        return this
    }

    Map toJsonSchema() {
        def schema = [type: type]
        schema << propSpec
        schema << getPropsJsonSchema()
        if (itemsSpec) {
            schema.items = itemsSpec instanceof PropertyDefinition ? itemsSpec.toJsonSchema() : [ type: itemsSpec ]
            schema.additionalProperties = false
        }

        return schema;
    }
}
