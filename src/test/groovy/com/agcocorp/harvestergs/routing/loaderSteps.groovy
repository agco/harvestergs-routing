package com.agcocorp.harvestergs.routing

import com.agcocorp.harvestergs.routing.loaders.ResourceLoader
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.main.JsonSchemaFactory
import groovy.json.JsonOutput
import cucumber.api.PendingException
import groovy.json.JsonSlurper

import static cucumber.api.groovy.EN.*
import groovyx.net.http.RESTClient
import groovyx.net.http.*

def resources = []
def comments = [
    [ body: 'First!', author: [ name: 'John Doe', email: 'john@doe.com' ], tags: [ [name: 'TEST'], [name: 'DUMMY'] ]],
    [ body: 'Really, John?', author: [ name: 'Jane Doe', email: 'jane@doe.com' ], tags: [ [name: 'TEST'], [name: 'DUMMY'] ]],
    [ body: 'Next!', author: [ name: 'Jack Doe', email: 'jack@doe.com' ], tags: [ [name: 'TEST'], [name: 'DUMMY'] ]],
]

def postComment = comments[2]
def patchComment = comments[1]
def getComment = comments[0]

Given(~/^a set of related resources$/) { ->
    def commentResource = new Resource()

    commentResource
        .definitions
        .Comment {
        properties {
            body {
                type 'string'
                description 'Comments contents'
            }

            author {
                type 'object'
                properties {
                    name { type 'string'}
                    email { type 'string'}
                    url { type 'string'}
                }
                required 'name', 'email'
            }

            tags {
                type 'array'
                items {
                    type 'object'
                    properties {
                        name { type 'string' }
                        size { type 'integer' }
                    }
                    required 'name'
                }
            }
        }
        required 'body'
    }

    commentResource
        .paths
        ."/comments" {
        get { req, res ->
            return comments
        }

        post { req, res ->
            return req.data
        }.document { docs ->
            docs.description = "Description for comments.post"
            docs
        }
        .skipAuth
            .skipValidation

        "/:id" {
            get    {req, res -> return getComment}
            patch  {req, res -> return req.data }
            //.document { docs -> docs.operationId = "commentUpdate"; docs }
            delete {req, res -> return null }
        }
    }

    resources << commentResource
}

Given(~/^these resources are loaded into an API$/) { ->
    def loader = new ResourceLoader([ "title": "testApp" ])
    resources.each {
        loader.loadResource it
    }
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

When(~/^I post a resource that is missing mandatory fields$/) { ->
    // Write code here that turns the phrase above into concrete actions
    def resource = [ author: [ name: 'John Doe' ] ]
    try {
        response = client.post(path: '/comments', requestContentType: ContentType.JSON, body: resource)
        fail("HTTP action should have returned an error")
    }
    catch(HttpResponseException e) {
        error = e
    }
}

Then(~/^I receive a (\d+) code$/) { code ->
    assert error.statusCode.toString() == code
}

def msg

Then(~/^the response is a valid jsonapi error$/) { ->
    assert error.response.responseData
    msg = error.response.responseData
    assert msg.id
    assert msg.title
    assert msg.detail
}

Then(~/^the details list all missing fields$/) { ->
    assert msg.detail.contains('body')
}

When(~/^I get the documentation for it$/) { ->
    response = client.get(path: '/swagger', requestContentType: ContentType.JSON)
}

Then(~/^the response correctly describes the resource$/) { ->
    assert response
    assert response.responseData
    response.responseData.with {
        assert swagger == "2.0"
        assert info.version == "0.1.0"
        assert info.title == "testApp"

        assert definitions."Comment"
        definitions."Comment".with {
            assert properties
            assert required
        }

        assert paths."/comments"
        paths."/comments".with {
            assert get
            assert get.description ==
                    "Returns a collection of comments the user has access to."
        }

        assert paths."/comments/:id"
        paths."/comments/:id".with {
            assert patch.parameters[0].description ==
                    "The comment JSON you want to update"
            assert patch
            assert get
            assert patch
            assert delete
        }
    }
}

When(~/^I run a (\w+) at path (.+)$/) { verb, path ->
    def body = targets[path][verb]
    try {
        response = client."$verb"(path: path, requestContentType: ContentType.JSON, body: body)
    }
    catch (HttpResponseException e) {
        println "Error: ${ JsonOutput.toJson(e.response.responseData) }"
        throw e
    }
}

Then(~/^I receive a (\d+) response code$/) { int code ->
    assert response.status == code
}

def slurper = new JsonSlurper()

Then(~/^the response message is (.+)/) { messageContents ->
    switch (messageContents) {
        case "a list":
            assert response.responseData == comments
            break
        case "the updated resource":
            assert response.responseData == patchComment
            break
        case "a single resource":
            assert response.responseData == getComment
            break
        case "the new resource":
            assert response.responseData == postComment
            break
        case "empty":
            assert ! response.responseData
            break
        default:
            throw new PendingException()
    }
}

def jsonSchemaFactory = JsonSchemaFactory.byDefault()
def objectMapper = new ObjectMapper()

Then(~/^it is swagger-compliant response$/) { ->
    def schema = jsonSchemaFactory.getJsonSchema("resource:/com/agcocorp/harvestergs/routing/swagger-schema.json")
    def data = objectMapper.valueToTree(response.responseData)
    def valResults = schema.validate(data)
    assert valResults.isSuccess()
}
