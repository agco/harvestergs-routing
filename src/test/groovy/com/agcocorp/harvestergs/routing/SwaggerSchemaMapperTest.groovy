// todo: uncomment after refactoring is done
package com.agcocorp.harvestergs.routing

import spock.lang.Specification
import spock.lang.*
import com.agcocorp.harvestergs.routing.loaders.SwaggerSchemaMapper

class SwaggerSchemaMapperTest extends Specification {
    final testData = [
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
        'array schema': [
            attributes: [
                body: [ type: 'string'],
                author: [
                    attributes: [
                        name: [ type: 'string' ],
                        email: [ type: 'string']
                    ]
                ],
                tags: [
                    type: 'array',
                    items: [
                        type: 'object',
                        attributes: [
                            name: [ type: 'string' ],
                            size: [ type: 'integer' ]
                        ]
                    ]
                ]
            ]
        ],
        'array swagger': [
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
                                ],
                                tags: [
                                    type: 'array',
                                    items: [
                                        type: 'object',
                                        properties: [
                                            name: [ type: 'string' ],
                                            size: [ type : 'integer']
                                        ]
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

    def testMatch(Map schema, Map swagger) {
        def sut = new SwaggerSchemaMapper()
        assert sut.map(schema) == swagger
        true
    }

    def "jsonapi schema to swagger mapping"(String schema, String swagger) {
        expect:
            testMatch(testData[schema], testData[swagger])

        where:
            schema              |      swagger
            'empty schema'      |   'empty swagger'
            'simple schema'     |   'simple swagger'
            'nested schema'     |   'nested swagger'
            'deep schema'       |   'deep swagger'
            'array schema'      |   'array swagger'
    }
}
