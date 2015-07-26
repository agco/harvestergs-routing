package com.agcocorp.harvestergs.routing

import javax.annotation.Resource

import static cucumber.api.groovy.EN.*
import static testHelpers.*
import cucumber.api.PendingException

def sut
def resources

Given(~/^a complete API definition$/) { ->
    sut = new ApiDefinition()
        .resources {
            post.attributes {
                title string
            }
            comment.attributes {
                body string
            }
        }
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

    // Write code here that turns the phrase above into concrete actions
    throw new PendingException()
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