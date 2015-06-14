package com.agcocorp.harvestergs.routing

class AttributeDefinition {
    @Delegate
    final TypeMapper mapper = new TypeMapper()

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
