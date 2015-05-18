Feature: Define a resource
  Scenario: Single valid resource
    Given a valid resource definition
    When it is fully defined
    Then it correctly maps into a set of objects