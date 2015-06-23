package com.agcocorp.harvestergs.routing

class VerbDefinition {
    final Closure handler
    private Closure document
    public Closure getDocument() { return document }
    final additionalFlags = [:]

    VerbDefinition(Closure handler) {
        this.handler = handler
    }

    def getSkipAuth() {
        additionalFlags['skipAuth'] = true
        this
    }

    def getSkipValidation() {
        additionalFlags['skipValidation'] = true
        this
    }

    def document(Closure closure) {
        this.document = closure
        this
    }
}
