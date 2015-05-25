import cucumber.api.PendingException
import groovy.json.JsonSlurper

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
    msg.detail.contains('name')
}

When(~/^I get the documentation for it$/) { ->
    response = client.get(path: '/swagger', requestContentType: ContentType.JSON)
    //throw new PendingException()
}

Then(~/^I receive a swagger-compliant response$/) { ->
    // Write code here that turns the phrase above into concrete actions
    assert response
    assert response.responseData
    throw new PendingException()
}

Then(~/^the response correctly describes the resource$/) { ->
    // Write code here that turns the phrase above into concrete actions
    throw new PendingException()
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