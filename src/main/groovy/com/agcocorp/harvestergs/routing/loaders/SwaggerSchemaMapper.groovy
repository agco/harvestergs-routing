package com.agcocorp.harvestergs.routing.loaders

class SwaggerSchemaMapper {
    private setInnerProp(obj, prop, value) {
        def current = obj
        def props = prop.tokenize('.')
        def last = props.size() - 1

        props.eachWithIndex {it, idx ->
            if (idx == last) {
                current[it] = value
            }
            else {
                if (current[it] == null) {
                    current[it] = [:]
                }
                current = current[it]
            }
        }
    }

    private setIfNotNull(obj, prop, value) {
        if (value) {
            setInnerProp obj, prop, value
        }
    }

    private setNotNull(obj, prop, value) {
        if (!value) {
            // todo: proper exception throw here
            throw new RuntimeException()
        }
        setInnerProp obj, prop, value
    }

    def map(schema) {
        def swagger = [ properties: [ data: [:] ] ]
        def data = swagger.properties.data
        setIfNotNull data, 'properties.type', schema.type
        if (schema.attributes) {
            setNotNull data, 'properties.attributes', [properties:[:]]
            def attr = data.properties.attributes
            attr.type = 'object'
            schema.attributes.each {
                attr.properties[it.key] = it.value
            }
        }
        return swagger
    }
}
