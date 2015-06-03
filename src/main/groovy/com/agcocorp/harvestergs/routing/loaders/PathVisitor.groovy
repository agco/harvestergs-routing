package com.agcocorp.harvestergs.routing.loaders

import com.agcocorp.harvestergs.routing.Path
import com.agcocorp.harvestergs.routing.PathSpec

class PathVisitor {
    def visitPath(Path pathSet, Closure visitor) {
        pathSet.paths.each { path ->
            recursePath path.value, path.key, visitor
        }
    }

    private def recursePath(PathSpec path, String pathName, Closure visitor) {
        visitor path, pathName

        path.children.each {
            recursePath it.value, pathName + it.key, visitor
        }
    }
}