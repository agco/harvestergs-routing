package com.agcocorp.harvestergs.routing

import groovy.transform.Canonical

@Canonical
class Definition {
    Map<String, Schema> schemas = [:]
    private mainSchemaName
    def getMainSchemaName() { mainSchemaName }

    def methodMissing(String name, args) {
        def schema = new Schema()
        if (! schemas) {
            mainSchemaName = name
        }
        schemas[name] = schema
        Definition.runClosure(args[0], schema, this)
        schemas
    }

    // todo: turn this into a first-class function, or at least extract
    static def runClosure(Closure closure, Object delegate, Object owner) {
        def code = closure.rehydrate(delegate, owner, owner)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
    }

    // todo: turn this into a first-class function, or at least extract
    static def setProperty(Object object, String property, Object value, boolean throwOnMiss = true) {
        if (object.hasProperty(property)) {
            object[property] = value
            return true
        }
        if (throwOnMiss) {
            throw new MissingPropertyException(property, object.class)
        }
        return false
    }
}
