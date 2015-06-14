package com.agcocorp.harvestergs.routing

import cucumber.api.PendingException
import static cucumber.api.groovy.EN.*
import static testHelpers.*

ResourceDefinition definition
def schema

Given(~/^a complete schema definition$/) { ->
    definition = new ResourceDefinition('person')
        .attributes {
            id uuid.description('User ID. Do not send it when posting or patching.')
            firstName string.required.description("User's first name")
            lastName string.required
            email email
            url string.pattern("urlPattern")
            avatars {
                small string.description('Url for the small version of the avatar')
                medium string.description('Url for the medium version of the avatar')
                large string.description('Url for the large version of the avatar')
            }
            tags arrayOf(string)
            socialHandles arrayOf {
                service string.required.description('Name of the social media service for this handle')
                url string.required
            }
        }
        .relationships {
            spouse person
            // todo: add 'reverse' support
            posts arrayOf(post)
        }
}

When(~/^I get its corresponding JSON schema$/) { ->
    schema = definition.toJsonSchema()
}

Then(~/^the schema correctly maps all definitions$/) { ->
    assert schema
    assertWith schema.person.properties.data.properties, {
        assertWith attributes.properties, {
            assert firstName == [type: 'string', description: "User's first name"]
            assert id == [ type: 'string', pattern: 'uuidPattern', description: 'User ID. Do not send it when posting or patching.' ]
            assert lastName == [type: 'string']
            assert email.type == 'string'
            assert email.pattern
            assert url.type == 'string'
            assert url.pattern
            assertWith avatars.properties, {
                assert small
                assert medium
                assert large
            }

            assertWith tags, {
                assert type == 'array'
                assert items == 'string'
            }

            assertWith socialHandles, {
                assert type == 'array'
                assertWith items, {
                    assert type == "object"
                    assertWith properties, {
                        assert service == [ type : 'string', description: 'Name of the social media service for this handle' ]
                        assert url == [ type : 'string' ]
                    }
                    assert required == [ 'service', 'url' ]
                }
            }
        }
        assert attributes.required == ['firstName', 'lastName']

        assertWith relationships.properties, {
            assertWith spouse.properties.data.properties, {
                assert type == [type: [enum: ['person']]]
                assert id == [type: 'string']
            }

            assertWith posts.properties.data, {
                assert type == 'array'
                assertWith items.properties, {
                    assert type == [type: [enum: ['post']]]
                    assert id == [type: 'string']
                }
            }
        }
    }
}

Then(~/^is a JSON schema compliant$/) { ->
    throw new PendingException()
}