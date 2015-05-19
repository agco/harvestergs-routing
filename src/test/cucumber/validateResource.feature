Feature: Validate Resource
  Scenario: Mandatory fields missing
    Given a valid resource definition
    When I post a resource that is missing mandatory fields
    Then I receive an error code
    And the message lists all missing fields