package com.agcocorp.harvestergs.routing

import groovy.transform.Canonical

@Canonical
class Relationship {
    String type
    String description
    Relationship items

    def methodMissing(String name, args) {
        switch (name) {
            case "items":
                items = new Relationship()
                Definition.runClosure(args[0], items, this);
                break;
            default:
                Definition.setProperty(this, name, args[0]);
        }
    }
}

