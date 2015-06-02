package com.agcocorp.harvestergs.routing
import spock.lang.*
import com.agcocorp.harvestergs.routing.loaders.SwaggerSchemaMapper

class SwaggerSchemaMapperTest extends Specification {
    def "test jsonapi schema to swagger mapping"(Map schema, swagger) {
        def sut = new SwaggerSchemaMapper()
        expect:
            sut.map(schema) == swagger

        where:
            schema                  |               swagger
            [:]                     | [ data: [:] ]
    }
}
