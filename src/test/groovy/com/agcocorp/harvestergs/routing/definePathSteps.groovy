package com.agcocorp.harvestergs.routing

import cucumber.api.PendingException
import static cucumber.api.groovy.EN.*

ResourceDefinition definition
def schema

Given(~/^a valid path definition$/) { ->
    // Write code here that turns the phrase above into concrete actions
    definition = new ResourceDefinition('person')
        .paths {
            '/people' {
                //todo: add document override support
                get { req, res -> 'people.get' }
                post { req, res -> 'people.post' }
                '/:id' {
                    get { req, res -> 'people/:id.get' }
                    patch { req, res -> 'people/:id.get' }
                    delete { req, res -> 'people/:id.get' }
                }
            }
        }
}

When(~/^I request its expanded list of paths$/) { ->
    // Write code here that turns the phrase above into concrete actions
    throw new PendingException()
}

Then(~/^I get a correct list of paths and handlers$/) { ->
    // Write code here that turns the phrase above into concrete actions
    throw new PendingException()
}


