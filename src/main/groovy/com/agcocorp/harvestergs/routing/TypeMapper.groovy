package com.agcocorp.harvestergs.routing

class TypeMapper {
    def PropertyDefinition getString() {
        return new PropertyDefinition('string')
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
        prop.pattern(/uuidPattern/)
        return prop
    }

    def PropertyDefinition getEmail() {
        def prop = new PropertyDefinition('string' )
        prop.pattern(/.+@.+/)
        return prop
    }

    def props = [:]


    def propertyMissing(String name) {
        return new PropertyDefinition(name)
    }

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
