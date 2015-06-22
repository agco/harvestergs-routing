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


    static deepCompare(m1, m2, currentPath = 'it') {
        def diffs = []
        if (m1 != m2) {
            def currentDiffs = [:]
            currentDiffs."$currentPath" = []
            def m1keys = m1 instanceof Map? m1*.key : null
            def m2keys = m2 instanceof Map? m2*.key : null
            if (m1keys && m2keys) {
                // todo: this key diff is computationally innefficient. Refactor if better speed is needed
                def m1only = m1keys - m2keys
                def m2only = m2keys - m1keys
                def common = m1keys - m1only - m2only

                m1only.each {
                    currentDiffs."$currentPath" << [ ["$it": m1[it]], null ]
                }
                m2only.each {
                    currentDiffs."$currentPath" << [ null, ["$it": m1[it]] ]
                }
                common.each {
                    def v1 = m1[it]
                    def v2 = m2[it]
                    if (v1 != v2) {
                        def deepDiff = deepCompare(m1[it], m2[it], "$currentPath.$it")
                        if (deepDiff) {
                            diffs << deepDiff
                        }
                    }
                }
            }
            else {
                currentDiffs."$currentPath" << [ m1, m2 ]
            }
            if (currentDiffs."$currentPath") {
                diffs << currentDiffs
            }
        }

        return diffs
    }
}
