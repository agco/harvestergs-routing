package com.agcocorp.harvestergs.routing

import groovy.transform.Canonical

@Canonical
class Schema {
    PropertyList attributes
    List<String> required
    String type
    String description
    Schema items

    def methodMissing(String name, args) {
        switch (name) {
            case "attributes":
                attributes = new PropertyList()
                Definition.runClosure(args[0], attributes, this);
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

