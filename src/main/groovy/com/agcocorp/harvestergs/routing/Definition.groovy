package com.agcocorp.harvestergs.routing

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
