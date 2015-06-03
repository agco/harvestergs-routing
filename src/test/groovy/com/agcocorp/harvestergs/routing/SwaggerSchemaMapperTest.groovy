package com.agcocorp.harvestergs.routing

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.main.JsonSchemaFactory
import spock.lang.*
import com.agcocorp.harvestergs.routing.loaders.SwaggerSchemaMapper

class SwaggerSchemaMapperTest extends Specification {
    def jsonSchemaFactory = JsonSchemaFactory.byDefault()
    def objectMapper = new ObjectMapper()
    final testData = [
        //'typed schema': [ type: 'comment' ],
        //'typed swagger': [ properties: [ data: [ properties: [ type: 'comment' ] ] ] ],
        'simple schema': [
            attributes: [
                body: [ type: 'string']
            ]
        ],
        'simple swagger': [
            properties: [
                data: [
                    properties: [
                        attributes: [
                            properties: [
                                body: [type: 'string' ]
                            ]
                        ]
                    ]
                ]
            ]
        ],
        'nested schema': [
            attributes: [
                body: [ type: 'string'],
                author: [
                    attributes: [
                        name: [ type: 'string' ],
                        email: [ type: 'string']
                    ]
                ]
            ]
        ],
        'nested swagger': [
            properties: [
                data: [
                    properties: [
                        // todo: add type and id
                        attributes: [
                            properties: [
                                body: [ type: 'string' ],
                                author: [
                                    properties: [
                                        name: [type: 'string'],
                                        email: [type: 'string']
                                    ]
                                ]
                            ]
                        ]
                    ]
                ]
            ]
        ],
        'deep schema': [
            attributes: [
                body: [ type: 'string'],
                author: [
                    attributes: [
                        name: [ type: 'string' ],
                        email: [ type: 'string'],
                        avatars: [
                            // todo: allow for null prop type
                            attributes: [
                                small: [ type: 'string'],
                                medium: [ type: 'string'],
                                large: [ type: 'string']
                            ]
                        ]
                    ]
                ]
            ]
        ],
        'deep swagger': [
            properties: [
                data: [
                    properties: [
                        attributes: [
                            properties: [
                                body: [ type: 'string' ],
                                author: [
                                    properties: [
                                        name: [type: 'string'],
                                        email: [type: 'string'],
                                        avatars: [
                                            properties: [
                                                small: [ type: 'string'],
                                                medium: [ type: 'string'],
                                                large: [ type: 'string']
                                            ]
                                        ]
                                    ]
                                ]
                            ]
                        ]
                    ]
                ]
            ]
        ],
        'empty schema': [ : ],
        'empty swagger': [ properties: [ data: [ : ] ] ]
    ]

    def checkSwagger(swagger) {
        def schema = jsonSchemaFactory.getJsonSchema("resource:/com/agcocorp/harvestergs/routing/swagger-schema.json")
        def data = objectMapper.valueToTree(swagger)
        def valResults = schema.validate(data)
        assert valResults.isSuccess()
    }

    def testMatch(Map schema, Map swagger) {
        def sut = new SwaggerSchemaMapper()
        assert sut.map(schema) == swagger
        true
    }

    def "jsonapi schema to swagger mapping"(String schema, swagger) {
        def sut = new SwaggerSchemaMapper()
        expect:
            testMatch(testData[schema], testData[swagger])
            //checkSwagger(swagger)

        where:
            schema              |      swagger
            'empty schema'      |   'empty swagger'
            //'typed schema'      |   'typed swagger'
            'simple schema'     |   'simple swagger'
            'nested schema'     |   'nested swagger'
            'deep schema'       |   'deep swagger'
    }
}