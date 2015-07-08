package com.agcocorp.harvestergs.routing

class ItemDefinition {
    def props = [:]

    def parseArgs(args) {
        switch (args[0].class) {
            case AttributeDefinition.class:
                return args[0]
            case Closure.class:
                def prop = new AttributeDefinition('object', args[0])
                return prop
            default:
                throw new RuntimeException("hey, I don't know what else to throw, I got a '${args[0].class}'!!")
        }
    }

    Map getPropsJsonSchema()
    {
        def schema = [:]
        def required = []
        if (props) {
            schema.properties = [:]
            props.each {
                schema.properties[it.key] = it.value.toJsonSchema()

                if (it.value.parentSpec['required']) {
                    required << it.key
                }
            }
            schema.additionalProperties = false

            if (required) {
                schema.required = required
            }
        }
        return schema;
    }

    def itemsSpec
    Map propSpec = [:]
    Map parentSpec = [:]

    AttributeDefinition getRequired() {
        parentSpec['required'] = true
        return this
    }

    AttributeDefinition getReadOnly() {
        propSpec['readOnly'] = true
        return this
    }

    AttributeDefinition pattern(String pattern) {
        propSpec['pattern'] = pattern
        return this
    }

    AttributeDefinition format(String format) {
        propSpec['format'] = format
        return this
    }

    AttributeDefinition description(String description) {
        propSpec['description'] = description
        return this
    }
}
