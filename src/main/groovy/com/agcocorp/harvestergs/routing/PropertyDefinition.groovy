package com.agcocorp.harvestergs.routing

class PropertyDefinition {
    final type
    @Delegate
    final TypeMapper mapper = new TypeMapper()

    private runClosure(Closure cl, delegate = this) {
        if (cl) {
            cl.delegate = delegate
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl.call()
        }
    }

    PropertyDefinition(String type, Closure cl = null, itemType = null) {
        this.type = type
        this.itemsSpec = itemType

        runClosure cl
    }

    def itemsSpec
    Map propSpec = [:]
    Map parentSpec = [:]

    PropertyDefinition getRequired() {
        parentSpec['required'] = true
        this
    }

    PropertyDefinition pattern(String pattern) {
        propSpec['pattern'] = pattern
        this
    }

    PropertyDefinition description(String description) {
        propSpec['description'] = description
        this
    }

    def methodMissing(String name, args) {
        props[name] = parseArgs(args)
        this
    }

    Map toJsonSchema() {
        def schema = [type: type]
        schema << propSpec
        schema << getPropsJsonSchema()
        if (itemsSpec) {
            schema.items = itemsSpec instanceof PropertyDefinition ? itemsSpec.toJsonSchema() : [ type: itemsSpec ]
        }

        return schema;
    }
}
