import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.main.JsonSchemaFactory
import cucumber.api.PendingException
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import com.fasterxml.jackson.annotation.JsonInclude.Include;


import static cucumber.api.groovy.EN.*
import groovyx.net.http.RESTClient
import groovyx.net.http.*
import static groovyx.net.http.ContentType.JSON

def client = new RESTClient('http://localhost:4567')
def targets = [
        "comments" : [
                "get" : null,
                "post": [ body: 'foobar' ],
        ],
        "comments/1": [
                "get": null,
                "patch": [ body: 'test'],
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
    def resource = '{}'
    try {
        response = client.post(path: '/comments', requestContentType: ContentType.JSON)
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
    assert response.responseData.swagger == "2.0"
    assert response.responseData.info.version == "0.1.0"
    assert response.responseData.info.title == "testApp"
    assert response.responseData.paths."/comments"
    assert response.responseData.paths."/comments".get
    assert response.responseData.paths."/comments".get.description ==
            "Returns a collection of comments the user has access to."
    assert response.responseData.paths."/comments/:id".patch.parameters[0].description ==
            "The comment JSON you want to update"
    assert response.responseData.paths."/comments/:id"
    assert response.responseData.paths."/comments/:id".patch
    assert response.responseData.paths."/comments/:id".get
    assert response.responseData.paths."/comments/:id".patch
    assert response.responseData.paths."/comments/:id".delete
}

When(~/^I run a (\w+) at path (.+)$/) { verb, path ->
    def body = targets[path][verb]
    response = client."$verb"(path: path, requestContentType: ContentType.JSON, body: body)
}

Then(~/^I receive a (\d+) response code$/) { int code ->
    assert response.status == code
}

Then(~/^the response message is (.+)/) { messageContents ->
    switch (messageContents) {
        case "a list":
            assert response.responseData == "comments.get"
            break
        case "the updated resource":
            assert response.responseData == "comments/1.patch"
            break
        case "a single resource":
            assert response.responseData == "comments/1.get"
            break
        case "the new resource":
            assert response.responseData == "comments.post"
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
    def schema = jsonSchemaFactory.getJsonSchema("resource:/swagger-schema.json")
    def data = objectMapper.valueToTree(response.responseData)
    def valResults = schema.validate(data)
    assert valResults.isSuccess()
}

