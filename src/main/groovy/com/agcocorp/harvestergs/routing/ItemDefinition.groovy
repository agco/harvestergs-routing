package com.agcocorp.harvestergs.routing

class ItemDefinition {
    def props = [:]

    def parseArgs(args) {
        switch (args[0].class) {
            case PropertyDefinition.class:
                return args[0]
            case Closure.class:
                def prop = new PropertyDefinition('object', args[0])
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
}
