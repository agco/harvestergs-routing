package com.agcocorp.harvestergs.routing

class AttributeSetDefinition extends TypeMapper {
    def methodMissing(String name, args) {
        props[name] = parseArgs(args)
        this
    }

    def toJsonSchema() {
        if (props) {
            return [attributes: getPropsJsonSchema()]
        }
        return [:]
    }
}
