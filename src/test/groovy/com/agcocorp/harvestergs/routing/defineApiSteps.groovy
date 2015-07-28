package com.agcocorp.harvestergs.routing

import javax.annotation.Resource

import static cucumber.api.groovy.EN.*
import static testHelpers.*
import cucumber.api.PendingException

def sut
def resources

Given(~/^a complete API definition$/) { ->
    // PLEASE NOTE:  the code below is the exact example at the README. This
    // guarantees the sample code always builds

    // defining an API
    def api = new ApiDefinition({
        port(1337)
        auth { req, res ->
            if (!req.headers('Authorization')) {
                // 'error' has helper methods for sending error status codes
                error.unauthorized()
            }
        }
        apiResources {
            post {
                attributes {
                    // defining a 'title' property. It is a string with a maximum
                    // 100 chars length and is mandatory
                    title string.required.maxLength(100)
                    body string.required.maxLength(4000)
                    status enumOf([draft, published, howto])
                    tags arrayOf(string)
                }
                // relationships (which establish links to other resources) are
                // defined in this special section
                relationships {
                    // establishing a relationship to a person resource. It is
                    // mandatory and is called 'person'
                    author person.required
                    // the paths section is where API operations are defined
                }
                paths {
                    // all paths must start with a '/' (slash)
                    "/posts" {
                        // this tells the loader that clients can get and post against the
                        // /posts endpoint
                        // the req and res arguments come from java spark
                        get { req, res -> return "A post list should be returned here." }
                        post { req, res -> return "This closure should implement the creation of posts." }
                        // all closures in the section below correspond to http verbs at the
                        // /posts/:id endpoint
                        "/:id" {
                            get { req, res -> return "A specific post should be returned here." }
                            // please notice that, as per JSON API specs, PATCH is used for updates
                            patch { req, res -> return "Implement here the code to update posts." }
                            delete { req, res -> return "Implement here the code to delete a posts." }
                        }
                    }
                }
            }
            // defining another resource, called 'comment'. Pretty barebones,
            // in order to keep this example brief
            comment {
                attributes {
                    body string
                }
                relationships {
                    author person
                }
            }
            // third resource definition
            person {
                attributes {
                    name string.required
                    email email.required
                }
            }
        }
    })

    sut = api
}

When(~/^I get its resources and attributes$/) { ->
    resources = sut.getAllResources()
}

Then(~/^I get a complete, correct list$/) { ->
    assert resources
    assert resources.post
    assert resources.post.class == ResourceDefinition.class
    assert resources.comment
    assert resources.comment.class == ResourceDefinition.class
    assert sut.authClosure
}

def builders = []

Given(~/^a set of resource builders$/) { ->
    builders << new CommentResourceBuilder({ null }, { null })
    builders << new PostResourceBuilder({ null }, { null })
}

When(~/^I define an API using them$/) { ->
    sut = new ApiDefinition().addResources(
        builders.collect {
            it.build()
        }
    )
}

Then(~/^I get a correct list, with all builder results$/) { ->
    resources = sut.getAllResources()
    assert resources
    assert resources.comment
    assert resources.post
}