package com.agcocorp.harvestergs.routing

class ItemDefinition {
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
