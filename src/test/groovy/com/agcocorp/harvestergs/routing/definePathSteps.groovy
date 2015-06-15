package com.agcocorp.harvestergs.routing

import static cucumber.api.groovy.EN.*
import static testHelpers.*

ResourceDefinition definition
def paths

Given(~/^a valid path definition$/) { ->
    definition = new ResourceDefinition('person')
        .paths {
            '/people' {
                //todo: add document override support
                get { req, res -> 'people.get' }
                    .document {
                        it.description = "people.get overriden description"
                        return it
                    }
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
    paths = definition.getAllPaths()
}

Then(~/^I get a correct list of paths and handlers$/) { ->
    assert paths
    assertWith paths['/people'], {
        assertWith it['get'], {
            assert handler
            assert document
            assert document([:]) == [ description: 'people.get overriden description' ]
        }
        assert post.handler
    }

    assertWith paths['/people/:id'], {
        assert get.handler
        assert patch.handler
        assert delete.handler
    }
}


