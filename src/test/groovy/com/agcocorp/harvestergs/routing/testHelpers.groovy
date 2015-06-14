package com.agcocorp.harvestergs.routing

class testHelpers {
    static assertWith(who, Closure cl) {
        assert who
        // using delegate to enforce 'with' behavior
        cl.delegate = who
        cl.resolveStrategy = Closure.DELEGATE_FIRST

        // passing who, so 'it' can be used, for whatever reason
        cl.call(who)
    }
}
