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
        return new AttributeDefinition('array', null, itemType.type)
    }

    def AttributeDefinition arrayOf(Closure itemDefinition) {
        def innerProp = new AttributeDefinition('object', itemDefinition)
        return new AttributeDefinition('array', null, innerProp)
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

    def AttributeDefinition enumOf(Closure cl) {
        def prop = new AttributeDefinition('enum', cl)
        return prop
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
}
