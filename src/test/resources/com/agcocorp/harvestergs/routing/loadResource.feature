Feature: Load a resource
  Background:
    Given a set of related resources
    And these resources are loaded into an API

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

  Scenario: Mandatory fields missing
    Given the aforementioned resource definition
    When I post a resource that is missing mandatory fields
    Then I receive a 400 code
    And the response is a valid jsonapi error
    And the details list all missing fields