package com.agcocorp.harvestergs.routing

import com.agcocorp.harvestergs.routing.loaders.SparkLoader
import com.agcocorp.harvestergs.routing.loaders.SwaggerLoader
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.main.JsonSchemaFactory
import groovy.json.JsonOutput
import cucumber.api.PendingException
import groovy.json.JsonSlurper

import static cucumber.api.groovy.EN.*
import groovyx.net.http.RESTClient
import groovyx.net.http.*
import static testHelpers.*


def resources = []
def comments = [
    [ body: 'First!', author: [ name: 'John Doe', email: 'john@doe.com' ], tags: [ [name: 'TEST'], [name: 'DUMMY'] ]],
    [ body: 'Really, John?', author: [ name: 'Jane Doe', email: 'jane@doe.com' ], tags: [ [name: 'TEST'], [name: 'DUMMY'] ]],
    [ body: 'Next!', author: [ name: 'Jack Doe', email: 'jack@doe.com' ], tags: [ [name: 'TEST'], [name: 'DUMMY'] ]],
]

def postComment = comments[2]
def patchComment = comments[1]
def getComment = comments[0]
def _requestData
def slurper = JsonSlurper.newInstance()
def responseData
def msg

Given(~/^a set of related resources$/) { ->
    def commentBuilder = new CommentResourceBuilder( { comments }, { getComment })
    def postBuilder = new PostResourceBuilder( { null }, { null })
    def dummyBuilder = new DummyResourceBuilder()
    resources = [ commentBuilder.build(), postBuilder.build(), dummyBuilder.build() ]
}

Given(~/^these resources are loaded into an API$/) { ->
    def loader = new SparkLoader()
    loader.loadResources resources
    def documenter = new SwaggerLoader([ "title": "testApp" ])
    documenter.loadDocs resources
}

def client = new RESTClient('http://localhost:4567')
def targets = [
        "comments" : [
                "get" : null,
                "post": postComment,
        ],
        "comments/1": [
                "get": null,
                "patch": patchComment,
                "delete": null
        ]
]

def response

Given(~/^the aforementioned resource definition$/) { ->
    // no action needed here -- all the setup occurred in the background steps
}

def error

def executeOperation(Closure operation) {
    response = error = null
    try {
        response = operation()
    }
    catch(HttpResponseException e) {
        error = e
    }
}

When(~/^I post a resource that is missing mandatory fields$/) { ->
    def resource = [data: [type: 'comment', attributes: [author: [name: 'John Doe']]]]

    response = error = null
    try {
        response = client.post(path: '/comments', requestContentType: ContentType.JSON, body: resource, headers: [my_fake_token: 'valid'])
        fail("HTTP action should have returned an error")
    }
    catch(HttpResponseException e) {
        error = e
    }
}

Then(~/^the response is a valid jsonapi error$/) { ->
    assert error.response.responseData
    msg = slurper.parse(error.response.responseData)
    assert msg.id
    assert msg.title
    assert msg.detail
}
//And the conforms the following regex <regex>
//Then(~/^the details list all missing fields$/) { ->
Then(~/^the conforms the following regex (.*)$/) { pattern ->
    //assert msg.detail.contains('body')
    assert msg.detail ==~ pattern
}

When(~/^I get the documentation for it$/) { ->
    response = error = null
    response = client.get(path: '/swagger', requestContentType: ContentType.JSON)
}

Then(~/^the response correctly describes the resource$/) { ->
    assert response
    assertWith msg,  {
        assert swagger == "2.0"
        assert info.version == "0.1.0"
        assert info.title == "testApp"

        assert definitions.comment
        assert definitions.post
        assert definitions.post.properties

        assert paths."/comments"
        paths."/comments".with {
            assert get
            assert get.description ==
                    "Returns a collection of comments the user has access to."
            assert post.description == "Custom description for comments.post"
        }

        assert paths."/comments/:id"
        paths."/comments/:id".with {
            assert patch.parameters[0].description ==
                    "The comment JSON you want to update"
            assert patch
            assertWith get, {
                assert description == "Returns the comment with the provided id, if it exists."
            }
            assert patch
            assert delete
        }

        def expectedSchema = [
            properties: [
                data: [
                    properties: [
                        type: [
                            enum: [ 'comment' ]
                        ],
                        id: [
                            type: 'string',
                            pattern: /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/
                        ],
                        attributes: [
                            properties: [
                                author: [
                                    properties: [
                                        email: [ type: 'string', format: 'email' ],
                                        name:  [ type: 'string', pattern: /.+ .+/ ],
                                        url:   [ type: 'string', format: 'uri' ]
                                    ],
                                    required: [ 'name', 'email' ],
                                    type: 'object',
                                    additionalProperties: false
                                ],
                                body: [
                                    description: 'Comments contents',
                                    type: 'string',
                                    maxLength: 4000,
                                    minLength: 1
                                ],
                                tags: [
                                    items: [
                                        properties: [
                                            name: [type: 'string' ],
                                            size: [type: 'integer', readOnly: true ]
                                        ],
                                        required: [ 'name' ],
                                        type: 'object',
                                        additionalProperties: false
                                    ],
                                    type: 'array',
                                    additionalProperties: false
                                ],
                                kind: [
                                    enum: [ 'classic', 'picture', 'howto' ]
                                ]
                            ],
                            required: ['body'],
                            additionalProperties: false
                        ],
                        relationships: [
                            properties: [
                                post: [
                                    description: "Owning post",
                                    properties: [
                                        data: [
                                            properties: [
                                                type: [enum: ['posts']],
                                                id: [type: 'string' ]
                                            ],
                                            additionalProperties: false
                                        ]
                                    ],
                                    additionalProperties: false
                                ]
                            ],
                            required: ['post'],
                            additionalProperties: false
                        ]
                    ],
                    additionalProperties: false
                ]
            ]
        ]

        assert definitions.comment == expectedSchema :
            JsonOutput.prettyPrint(JsonOutput.toJson(deepCompare(expectedSchema, definitions.comment)))

        assertWith definitions.post.properties.data.properties, {
            assert id == [type: 'string', description: 'url-encoded version of the tile, for easy permalinks']
            assertWith attributes.properties, {
                assert title == [ type: 'string' ]
                assert body.type == 'string'
                assert tags
                assert createdOn == [ type: 'string', format: 'date-time' ]
                assert published == [ type: 'boolean' ]
                assertWith coordinates.properties, {
                    assert latitude
                    assert longitude
                }
            }
        }
    }
}

When(~/^I run a (\w+) at path (.+)$/) { verb, path ->
    def body = targets[path][verb]
    response = error = null
    try {
        response = client."$verb"(path: path, requestContentType: ContentType.JSON, body: body, headers:[my_fake_token: 'valid'])
    }
    catch (HttpResponseException e) {
        println "Error: ${ JsonOutput.toJson(e.response.responseData) }"
        throw e
    }
}

Then(~/^I receive a (\d+) response code$/) { String code ->
    if (response) {
        assert response.status.toString() == code
    }
    else {
        assert error.statusCode.toString() == code
    }
}

Then(~/^the response message is (.+)/) { messageContents ->
    def data = response.responseData?
        slurper.parse(response.responseData) : null
    switch (messageContents) {
        case "a list":
            assert data == comments
            break
        case "a single resource":
            assert data == getComment
            break
        case "the new resource":
            assert data == postComment
            break
        case "the updated resource":
            assert data == patchComment
            break
        case "empty":
            assert ! data
            break
        default:
            throw new PendingException()
    }
}

def jsonSchemaFactory = JsonSchemaFactory.byDefault()
def objectMapper = new ObjectMapper()

Then(~/^it is swagger-compliant response$/) { ->
    def schema = jsonSchemaFactory.getJsonSchema("resource:/com/agcocorp/harvestergs/routing/swagger-schema.json")
    msg = slurper.parse(response.responseData)
    def data = objectMapper.valueToTree(msg)
    def valResults = schema.validate(data)
    assert valResults.isSuccess()
}

When(~/^I try to acess the API with a (.*) auth token$/) { tokenScenario ->
    def headers = [
        'invalid': [my_fake_token: 'invalid'],
        'valid': [my_fake_token: 'valid'],
        'missing': [:]
    ]

    response = error = null
    try {
        response = client.get(path: '/comments', requestContentType: ContentType.JSON, headers: headers[tokenScenario])
    }
    catch (HttpResponseException e) {
        error = e
    }
}


Given(~/^a resource that violates the (.+) rule$/) { violationCase ->
    // step left blank -- no setup needed here
}

Given(~/^containing these attributes (.+)$/) { String attrJson ->
    def attrData = slurper.parseText(attrJson)
    _requestData = [
        data: [
            attributes: attrData
        ]
    ]
}

When(~/^I post it at the (.+) endpoint$/) { path ->
    response = error = null
    try {
        response = client.post(path: path, requestContentType: ContentType.JSON, body: _requestData, headers:[my_fake_token: 'valid'])
        fail("HTTP action should have returned an error")
    }
    catch(HttpResponseException e) {
        error = e
    }
}

Then(~/^the response content-type is "(.*?)"$/) { String contentType ->
    // Write code here that turns the phrase above into concrete actions
    assert response
    def expectedContentType = "Content-Type: $contentType".toString()
    assert expectedContentType == response.headers['Content-Type'].toString()
}
