package com.agcocorp.harvestergs.routing

import groovy.transform.Canonical

@Canonical
class Path {
    Map<String, PathSpec> paths = [:]
    private _rootPathEndpoint
    def getrootPathEndpoint() { _rootPathEndpoint }

    def methodMissing(String name, args) {
        def spec = new PathSpec()
        if (! paths) {
            _rootPathEndpoint = name
        }

        paths[name] = spec
        Definition.runClosure args[0], spec, this
        paths
    }
}
