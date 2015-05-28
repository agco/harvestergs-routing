package com.agcocorp.harvester.routing

import cucumber.api.PendingException
import groovy.json.JsonSlurper
import static cucumber.api.groovy.EN.*

def sut
def input
def target

def defineSchema(builder) {
    builder.Comment {
        properties {
            id {
                type 'integer'
                description 'The comment id'
            }
            name {
                type 'string'
                description 'The comment name'
            }
        }
        required 'name'
    }
}
def defineNestedSchema(builder) {
    builder.Comment {
        properties {
            id {
                type 'integer'
                description 'The comment id'
            }
            name {
                type 'string'
                description 'The comment name'
            }
            pingback {
                type 'object'
                properties {
                    author {
                        type 'object'
                        properties {
                            name {
                                type 'string'
                            }
                            email {
                                type 'string'
                            }
                        }
                    }
                    quote {
                        type 'string'
                    }
                }
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

def checkSchema(schema) {
    assert schema.Comment.properties.size() == 2
    assert schema.Comment.properties.id.type == 'integer'
}

def checkNestedSchema(schema) {
    assert schema.Comment.properties.size() == 3
    assert schema.Comment.properties.id.type == 'integer'
    assert schema.Comment.properties.pingback
    assert schema.Comment.properties.pingback.properties.size() == 2
    assert schema.Comment.properties.pingback.properties.quote.type == 'string'
    assert schema.Comment.properties.pingback.properties.author.type == 'object'
    assert schema.Comment.properties.pingback.properties.author.properties.size() == 2
    assert schema.Comment.properties.pingback.properties.author.properties.email.type == 'string'
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

def createSut(entity) {
    switch (entity) {
        case ~/.*schema/ : return new Definition()
        case "path": return new Path()
        case "resource": return new Resource()
        default: throw new PendingException()
    }
}

Given(~/^a valid *(.+) definition$/) { entity ->
    sut = createSut(entity)
    target = entity
}

When(~/^it is fully defined/) { ->
    switch (target) {
        case "schema":
            input = defineSchema(sut)
            break
        case "nested schema":
            input = defineNestedSchema(sut)
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

Then(~/it correctly maps into a set of objects/) { ->
    switch (target) {
        case "schema":
            checkSchema(input)
            break
        case "nested schema":
            checkNestedSchema(input)
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
