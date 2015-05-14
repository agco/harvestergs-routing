
def d = new Definition().Comment {
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


assert d.Comment.properties.size() == 2
assert d.Comment.properties.id.type == 'Integer'

println d

def p = new Path()."/comments" {
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

assert p."/comments"
assert p."/comments".get.run(null, null) == "comments.GET"
assert p."/comments".post.run(null, null) == "comments.POST"
assert p."/comments".post.document.run([ summary: "Summary for comments.POST"]) ==
        [ summary: "Summary for comments.POST", description: "Description for comments.POST"]

assert p."/comments".children."/:id".get.run(null, null) == "comments/:id.GET"
assert p."/comments".children."/:id".patch.document.run([:]) == [ operationId: "commentUpdate" ]

assert p."/comments".post.flags.contains('skipAuth')
assert p."/comments".post.flags.contains('skipValidation')

println p

def spec = new Resource()
def s = new Resource()
        .definition {
    Comment {
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
            .path
}
.path {
    "/comments" {
        get { req, res ->
            "comments.get"
        }

        post { req, res ->
            "comments.post"
        }.document { docs ->
            docs.description = "Description for comments.POST"
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

assert s.definition
assert s.path

assert s.path.paths["/comments"]
assert s.path.paths["/comments"].get.run(null, null) == "comments.get"
assert s.path.paths["/comments"].post.run(null, null) == "comments.post"
assert s.path.paths["/comments"].post.document.run([ summary: "Summary for comments.POST"]) ==
        [ summary: "Summary for comments.POST", description: "Description for comments.POST"]

assert s.path.paths["/comments"].children."/:id".get.run(null, null) == "comments/1.get"
assert s.path.paths["/comments"].children."/:id".patch.document.run([:]) == [ operationId: "commentUpdate" ]

assert s.path.paths["/comments"].post.flags.contains('skipAuth')
assert s.path.paths["/comments"].post.flags.contains('skipValidation')

assert s.definition.schemas.Comment.properties.size() == 2
assert s.definition.schemas.Comment.properties.size() == 2
assert s.definition.schemas.Comment.properties.id.type == 'Integer'

println s

new RsrourceLoader().loadResource(s)

import groovyx.net.http.RESTClient
import groovyx.net.http.ContentType
import static groovyx.net.http.ContentType.JSON


def client = new RESTClient('http://localhost:4567')
def targets = [
        "comments" : [ "get", "post" ],
        "comments/1": ["get", "patch", "delete"]
]
targets.each { path ->
    path.value.each { verb ->
        def res = client."$verb"(path: path.key, requestContentType: ContentType.JSON)
        assert res.status == 200
        assert res.responseData == "${path.key}.${verb}"
    }
}

spark.Spark.stop()
