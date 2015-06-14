package com.agcocorp.harvestergs.routing

import cucumber.api.PendingException
import static cucumber.api.groovy.EN.*

ResourceDefinition definition
def paths

Given(~/^a valid path definition$/) { ->
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
    paths = definition.getPaths()
}

Then(~/^I get a correct list of paths and handlers$/) { ->
    throw new PendingException()
}


