import cucumber.api.PendingException
import groovy.json.JsonSlurper

import static cucumber.api.groovy.EN.*
import groovyx.net.http.RESTClient
import groovyx.net.http.*
import static groovyx.net.http.ContentType.JSON

def resources = []

Given(~/^a set of related resources$/) { ->
    // Write code here that turns the phrase above into concrete actions
    def commentResource = new Resource()
    commentResource.definitions =
            commentResource.definitions
            .Comments {
                properties {
                    body {
                        type 'string'
                        description 'Comments contents'
                    }
                }
                required 'body'
            }

    commentResource.paths =
        commentResource.paths
        ."/comments" {
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
                        //.document { docs -> docs.operationId = "commentUpdate"; docs }
                delete {req, res -> "comments/1.delete"}
            }
        }

    resources << commentResource
}

Given(~/^these resources are loaded into an API$/) { ->
    def loader = new ResourceLoader()
    resources.each {
        loader.loadResource it
    }
}
