package com.agcocorp.harvestergs.routing

class TypeMapper {
    static final UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/
    static final EMAIL_PATTERN = /^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,4}$/

    def PropertyDefinition getString() {
        return new PropertyDefinition('string')
    }

    def PropertyDefinition getBoolean() {
        return new PropertyDefinition('boolean')
    }

    def PropertyDefinition getNumber() {
        return new PropertyDefinition('number')
    }

    def PropertyDefinition getObject() {
        return new PropertyDefinition('object')
    }

    def PropertyDefinition getInteger() {
        return new PropertyDefinition('integer')
    }

    // todo: support advanced cases such as arrayof(arrayof(...)) and arrayof( { ... })
    def PropertyDefinition arrayOf(PropertyDefinition itemType) {
        return new PropertyDefinition('array', null, itemType.type)
    }

    def PropertyDefinition arrayOf(Closure itemDefinition) {
        def innerProp = new PropertyDefinition('object', itemDefinition)
        return new PropertyDefinition('array', null, innerProp)
    }

    def PropertyDefinition getUuid() {
        def prop = new PropertyDefinition('string' )
        prop.pattern(UUID_PATTERN)
        return prop
    }

    def PropertyDefinition getEmail() {
        def prop = new PropertyDefinition('string' )
        prop.pattern(EMAIL_PATTERN)
        return prop
    }

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

            if (required) {
                schema.required = required
            }
        }
        return schema;
    }
}
