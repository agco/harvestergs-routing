Feature: Load a resource
  Background:
    Given a set of related resources
    And these resources are loaded into an API

  Scenario Outline: API auth
     Given the aforementioned resource definition
      When I try to acess the API with a <case> auth token
      Then I receive a <code> response code

    Examples:
      | case    | code |
      | missing |  401 |
      | invalid |  403 |
      | valid   |  200 |

  Scenario Outline: Single valid resource
     Given the aforementioned resource definition
      When I run a <action> at path <path>
      Then I receive a <code> response code
       And the response message is <response>

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
    | rule      | attributes                     | regex |
    | required  | {"author":{"name":"John Doe"}} | (?s).*body.*      |
    | readOnly  | {"body":"body","author":{"name":"author","email":"a@e.com"},"tags":[{"name":"TAG","size":15}]} | (?s).*size.*|

