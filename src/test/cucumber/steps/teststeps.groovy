import cucumber.api.PendingException
import static cucumber.api.groovy.EN.*
import groovyx.net.http.RESTClient
import groovyx.net.http.ContentType
import static groovyx.net.http.ContentType.JSON


def sut
def input
def target

def client = new RESTClient('http://localhost:4567')
def targets = [
        "comments" : [ "get", "post" ],
        "comments/1": ["get", "patch", "delete"]
]


def createSut(entity) {
    switch (entity) {
        case "schema" : return new Definition()
        case "path": return new Path()
        case "resource": return new Resource()
        default: throw new PendingException()
    }
}

Given(~/^a valid (\w+) definition$/) { entity ->
    sut = createSut(entity)
    target = entity
}

def defineSchema(builder) {
    builder.Comment {
        properties {
            id {
                type 'Integer'
                description 'The comment id'
            }
            name {
                type 'String'
                description 'The comment name'
            }
        }
        required 'name'
    }
}

def definePath(builder) {
    builder."/comments" {
        get { req, res ->
            "comments.get"
        }

        post { req, res ->
            "comments.post"
        }.document { docs ->
            docs.description = "Description for comments.post"
            docs
        }
        .skipAuth
        .skipValidation

        "/:id" {
            get    {req, res -> "comments/1.get"}
            patch  {req, res -> "comments/1.patch"}
                    .document { docs -> docs.operationId = "commentUpdate"; docs }
            delete {req, res -> "comments/1.delete"}
        }
    }
}

def defineResource(builder) {
    def input = new Resource()
    input.definitions = defineSchema(builder.definitions)
    input.paths = definePath(builder.paths)
    input

}

When(~/^it is fully defined/) { ->
    switch (target) {
        case "schema":
            input = defineSchema(sut)
            break
        case "path":
            input = definePath(sut)
            break
        case "resource":
            input = defineResource(sut)
            break
        default:
            throw new PendingException()
    }
}

When(~/^it is loaded/) { ->
    input = defineResource(sut)
    new ResourceLoader().loadResource(input)
}

def checkSchema(schema) {
    assert schema.Comment.properties.size() == 2
    assert schema.Comment.properties.id.type == 'Integer'
}

def checkPath(path) {
    assert path."/comments"
    assert path."/comments".get.run(null, null) == "comments.get"
    assert path."/comments".post.run(null, null) == "comments.post"
    assert path."/comments".post.document.run([ summary: "Summary for comments.post"]) ==
            [ summary: "Summary for comments.post", description: "Description for comments.post"]

    assert path."/comments".children."/:id".get.run(null, null) == "comments/1.get"
    assert path."/comments".children."/:id".patch.document.run([:]) == [ operationId: "commentUpdate" ]

    assert path."/comments".post.flags.contains('skipAuth')
    assert path."/comments".post.flags.contains('skipValidation')
}

Then(~/it correctly maps into a set of objects/) { ->
    switch (target) {
        case "schema":
            checkSchema(input)
            break
        case "path":
            checkPath(input)
            break
        case "resource":
            checkSchema(input.definitions.schemas)
            checkPath(input.paths.paths)
            break
        default:
            throw new PendingException()
    }
}

Then(~/it correctly creates API endpoints/) { ->
    targets.each { path ->
        path.value.each { verb ->
            def res = client."$verb"(path: path.key, requestContentType: ContentType.JSON)
            assert res.status == 200
            assert res.responseData == "${path.key}.${verb}"
        }
    }
}

And(~/correctly documents them/) { ->
    //def res = client.get(path:'swagger', requestContentType: ContentType.JSON)
    throw new PendingException()
}

def response

When(~/^I post a resource that is missing mandatory fields$/) { ->
    // Write code here that turns the phrase above into concrete actions
    def resource = '{}'
    response = client.post(path: '/comments', requestContentType: ContentType.JSON)
    println response
}

Then(~/^I receive an error code$/) { ->
    // Write code here that turns the phrase above into concrete actions
    assert response.status == 400
}

Then(~/^the message lists all missing fields$/) { ->
    // Write code here that turns the phrase above into concrete actions
    throw new PendingException()
}


