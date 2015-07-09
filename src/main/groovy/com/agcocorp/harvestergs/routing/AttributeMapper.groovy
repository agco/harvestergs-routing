package com.agcocorp.harvestergs.routing

class AttributeMapper extends ItemDefinition {
    static final UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/

    def AttributeDefinition getString() {
        return new AttributeDefinition('string')
    }

    def AttributeDefinition getBool() {
        return new AttributeDefinition('boolean')
    }

    def AttributeDefinition getNumber() {
        return new AttributeDefinition('number')
    }

    def AttributeDefinition getObject() {
        return new AttributeDefinition('object')
    }

    def AttributeDefinition getInteger() {
        return new AttributeDefinition('integer')
    }

    // todo: support advanced cases such as arrayof(arrayof(...))
    def AttributeDefinition arrayOf(AttributeDefinition itemType) {
        return new AttributeDefinition('array', itemType.type)
    }

    def AttributeDefinition arrayOf(Closure itemDefinition) {
        def innerProp = new AttributeDefinition('object', itemDefinition)
        return new AttributeDefinition('array', innerProp)
    }

    def AttributeDefinition getUuid() {
        def prop = new AttributeDefinition('string')
        prop.pattern(UUID_PATTERN)
        return prop
    }

    def AttributeDefinition getEmail() {
        def prop = new AttributeDefinition('string')
        prop.format('email')
        return prop
    }

    def AttributeDefinition getDatetime() {
        def prop = new AttributeDefinition('string')
        prop.format('date-time')
        return prop
    }

    def AttributeDefinition getUri() {
        def prop = new AttributeDefinition('string')
        prop.format('uri')
        return prop
    }

    def AttributeDefinition enumOf(ArrayList enumValues) {
        def prop = new AttributeDefinition(enumValues)
        return prop
        //return arg
    }

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

    def props = [:]

    def getId() {
        return props['id']
    }

    Map getPropsJsonSchema()
    {
        def schema = [:]
        def required = []
        //def idSchema = [  ]
        if (props) {
            schema.properties = [:]
            props.each {
                if (it.key != 'id') {
                    schema.properties[it.key] = it.value.toJsonSchema()

                    if (it.value.parentSpec['required']) {
                        required << it.key
                    }

                }
            }
            schema.additionalProperties = false

            if (required) {
                schema.required = required
            }
        }
        return schema
    }
}
