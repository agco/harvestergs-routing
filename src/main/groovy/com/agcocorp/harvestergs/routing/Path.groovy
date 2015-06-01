package com.agcocorp.harvestergs.routing

import groovy.transform.Canonical

@Canonical
class Path {
    private builder

    def Path(builder = null) {
        this.builder = builder
    }

    Map<String, PathSpec> paths = [:]
    private _rootPathEndpoint
    def getrootPathEndpoint() { _rootPathEndpoint }

    def methodMissing(String name, args) {
        def spec = new PathSpec(parentPath: null, builder: builder)
        if (! paths) {
            _rootPathEndpoint = name
        }

        paths[name] = spec
        Definition.runClosure args[0], spec, this
        paths
    }
}
