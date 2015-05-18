Feature: Load a resource
  Scenario: Single valid resource
    Given a valid resource definition
    When it is loaded
    Then it correctly creates API endpoints
    And correctly documents them