package com.agcocorp.harvestergs.routing

class VerbHandlerSpec {
    final Closure handler
    private Closure document
    public Closure getDocument() { return document }

    VerbHandlerSpec(Closure handler) {
        this.handler = handler
    }

    def document(Closure closure) {
        this.document = closure
    }
}
