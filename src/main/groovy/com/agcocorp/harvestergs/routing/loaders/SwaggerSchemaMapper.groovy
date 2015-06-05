package com.agcocorp.harvestergs.routing.loaders

import com.fasterxml.jackson.databind.ObjectMapper

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
            // todo: throw proper exception here
            throw new RuntimeException()
        }
        setInnerProp obj, prop, value
    }

    private mapToSwagger(parent, level = 0) {
        def swagger = [:]

        //todo: switch this to parent.properties.each once the DSL objects start being used
        parent.each { name, value ->
            if (value) {
                switch (name) {
                    case 'relationships':
                        break;
                    case 'attributes':
                        def attr
                        if (level == 0) {
                            setNotNull swagger, 'properties.attributes', [properties: [:]]
                            attr = swagger.properties.attributes
                        } else {
                            swagger['properties'] = [:]
                            attr = swagger
                        }
                        //attr.type = 'object'
                        parent.attributes.each {
                            attr.properties[it.key] = mapToSwagger(it.value, level + 1)
                        }
                        break;
                    case 'items':
                        setIfNotNull swagger, 'items', mapToSwagger(value, level + 1)
                        break;
                    default:
                        if (level > 0) {
                            setIfNotNull swagger, "$name", value
                        } else {
                            setIfNotNull swagger, "properties.attributes.$name", value
                        }
                        break;
                }
            }
        }

        swagger
    }

    def map(schema) {
        // todo: converting to a map removes awkward closure handling -- but aren't there any better ideas?
        def s = new ObjectMapper().convertValue(schema, Map.class)
        def swagger = [
            properties: [
                data: mapToSwagger(s)
            ]
        ]
        return swagger
    }
}
