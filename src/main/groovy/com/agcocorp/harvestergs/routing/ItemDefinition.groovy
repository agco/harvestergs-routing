package com.agcocorp.harvestergs.routing

class ItemDefinition {
    def props = [:]

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

    def itemsSpec
    Map propSpec = [:]
    Map parentSpec = [:]

    ItemDefinition getRequired() {
        parentSpec['required'] = true
        return this
    }

    ItemDefinition getReadOnly() {
        propSpec['readOnly'] = true
        return this
    }

    ItemDefinition pattern(String pattern) {
        propSpec['pattern'] = pattern
        return this
    }

    ItemDefinition format(String format) {
        propSpec['format'] = format
        return this
    }

    ItemDefinition description(String description) {
        propSpec['description'] = description
        return this
    }
}
