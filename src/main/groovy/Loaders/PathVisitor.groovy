package com.agcocorp.harvester.routing

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