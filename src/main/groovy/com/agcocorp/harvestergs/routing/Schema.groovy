package com.agcocorp.harvestergs.routing

import groovy.transform.Canonical

@Canonical
class Schema {
    PropertyList properties
    List<String> required
    String type
    String description
    Schema items

    def methodMissing(String name, args) {
        switch (name) {
            case "properties":
                properties = new PropertyList()
                Definition.runClosure(args[0], properties, this);
                break;
            case "items":
                items = new Schema()
                Definition.runClosure(args[0], items, this);
                break;
            case "required":
                this.required = args
                break;
            default:
                Definition.setProperty(this, name, args[0]);
        }
    }
}

