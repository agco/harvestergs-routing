package com.agcocorp.harvester.routing

import groovy.transform.Canonical

@Canonical
class Definition {
    Map<String, Schema> schemas = [:]
    private _mainSchemaName
    def getmainSchemaName() { _mainSchemaName }

    def methodMissing(String name, args) {
        def schema = new Schema()
        if (! schemas) {
            _mainSchemaName = name
        }
        schemas[name] = schema
        Definition.runClosure(args[0], schema, this)
        schemas
    }

    // todo: turn this into a first-class function, or at least extract
    static def runClosure(Closure cl, Object delegate, Object owner) {
        def code = cl.rehydrate(delegate, owner, owner)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
    }

    // todo: turn this into a first-class function, or at least extract
    static def setProperty(Object obj, String property, Object value, boolean throwOnMiss = true) {
        if (obj.hasProperty(property)) {
            obj[property] = value
            return true
        }
        if (throwOnMiss) {
            throw new MissingPropertyException(property, obj.class)
        }
    }
}

@Canonical
class Schema {
    PropertyList properties = new PropertyList()
    List<String> required
    String type
    String description

    def methodMissing(String name, args) {
        switch (name) {
            case "properties":
                Definition.runClosure(args[0], properties, this);
                break;
            case "required":
                this.required = args
                break;
            default:
                Definition.setProperty(this, name, args[0]);
            //todo: throw here
        }
    }
}

@Canonical
class PropertyList extends HashMap<String, Schema> {
    def methodMissing(String name, args) {
        def prop = new Schema()
        Definition.runClosure(args[0], prop, this)
        this[name] = prop
    }
}
