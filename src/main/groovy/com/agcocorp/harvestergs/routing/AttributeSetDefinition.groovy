package com.agcocorp.harvestergs.routing

class AttributeSetDefinition extends AttributeMapper {
    def methodMissing(String name, args) {
        props[name] = parseArgs(args)
        this
    }

    def propertyMissing(String name) {
        return name
    }

    def toJsonSchema() {
        if (props) {
            return [attributes: getPropsJsonSchema()]
        }
        return [:]
    }
}
