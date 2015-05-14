import groovy.json.JsonOutput
import cucumber.api.PendingException
import static cucumber.api.groovy.EN.*
import groovyx.net.http.RESTClient
import groovyx.net.http.ContentType
import static groovyx.net.http.ContentType.JSON

def client = new RESTClient('http://localhost:9091')
def resp
def inputOrder
def error
def sut
def input
def target

def createSut(entity) {
    switch (entity) {
        case "schema" : return new Definition()
        case "path": return new Path()
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
        required 'id', 'name'
    }
}

def definePath(builder) {
    builder."/comments" {
        get { req, res ->
            "comments.GET"
        }

        post { req, res ->
            "comments.POST"
        }.document { docs ->
            docs.description = "Description for comments.POST"
            docs
        }
        .skipAuth
        .skipValidation

        "/:id" {
            get    {req, res -> "comments/:id.GET"}
            patch  {req, res -> "comments/:id.PATCH"}
                    .document { docs -> docs.operationId = "commentUpdate"; docs }
            delete {req, res -> "comments/:id.DELETE"}
        }
    }
}

When(~/^it is fully defined/) { ->
    switch (target) {
        case "schema":
            input = defineSchema(sut)
            break
        case "path":
            input = definePath(sut)
            break
    }
}

def checkSchema(schema) {
    assert schema.Comment.properties.size() == 2
    assert schema.Comment.properties.id.type == 'Integer'
}

def checkPath(path) {
    assert path."/comments"
    assert path."/comments".get.run(null, null) == "comments.GET"
    assert path."/comments".post.run(null, null) == "comments.POST"
    assert path."/comments".post.document.run([ summary: "Summary for comments.POST"]) ==
            [ summary: "Summary for comments.POST", description: "Description for comments.POST"]

    assert path."/comments".children."/:id".get.run(null, null) == "comments/:id.GET"
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
    }
}
