@load
Feature: Load a resource
  Background:
    Given a set of related resources
    And these resources are loaded into an API

  Scenario Outline: API auth
     Given the aforementioned resource definition
      When I try to access the API with a <case> auth token
      Then I receive a <code> response code

    Examples:
      | case    | code |
      | missing |  401 |
      | invalid |  403 |
      | valid   |  200 |

  Scenario: Skip auth
    Given the aforementioned resource definition
    When I try to access an endpoint configured with no auth
    Then I receive a 200 response code


  Scenario Outline: Single valid resource
     Given the aforementioned resource definition
      When I run a <action> at path <path>
      Then I receive a <code> response code
       And the response message is <response>
       And the response content-type is "application/vnd.api+json"

  Examples:
    | path       | action | code | response             |
    | comments   | get    |  200 | a list               |
    | comments   | post   |  201 | the new resource     |
    | comments/1 | patch  |  200 | the updated resource |
    | comments/1 | delete |  204 | empty                |
    | comments/1 | get    |  200 | a single resource    |

  Scenario: Standard docs generation
      Given the aforementioned resource definition
       When I get the documentation for it
       Then it is swagger-compliant response
        And the response correctly describes the resource

  Scenario Outline: Mandatory fields missing
      Given the aforementioned resource definition
        And a resource that violates the <rule> rule
        And containing these attributes <attributes>
       When I post it at the /comments endpoint
       Then I receive a 400 response code
        And the response is a valid jsonapi error
        And the conforms the following regex <regex>
  Examples:
    | rule      | regex         | attributes                                            |
    | required  | (?s).*body.*  | {"author":{"name":"John Doe"}}                        |
    | minLength | (?s).*body.*  | {"body":""}                                           |
    | pattern   | (?s).*name.*  | {"body":"b","author":{"name":"a","email":"a@e.com"}}  |
    | maxLength | (?s).*tags.*  | {"body":"b","tags":[{"name":"LOOOOOOOONG"}]}          |
    | maximum   | (?s).*coord.* | {"body":"b","coordinates":{"latitude":200}}           |
    | minimum   | (?s).*coord.* | {"body":"b","coordinates":{"latitude":-200}}          |

  Scenario: validation skip
      Given the aforementioned resource definition
       When I run a post command that bypasses standard validation
       Then I receive a 201 response code

