Feature: Load a resource
  Background:
    Given a set of related resources
    And these resources are loaded into an API

  Scenario: Single valid resource
    Given the aforementioned resource definition
    When I reach its REST endpoints
    Then I receive the correct messages in return

  Scenario: Standard docs generation
    Given the aforementioned resource definition
    When I get the documentation for it
    Then I receive a swagger-compliant response
    And the response correctly describes the resource

  Scenario: Mandatory fields missing
    Given the aforementioned resource definition
    When I post a resource that is missing mandatory fields
    Then I receive a 400 code
    And the response is a valid jsonapi error
    And the details list all missing fields