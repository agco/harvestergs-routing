Feature: Validate Resource
  Scenario: Mandatory fields missing
    Given a valid resource definition
    When I post a resource that is missing mandatory fields
    Then I receive a 400 code
    And the response is a valid jsonapi error
    And the details list all missing fields