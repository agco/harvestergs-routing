Feature: Define a schema
  Scenario: Single valid schema
    Given a valid schema definition
    When it is fully defined
    Then it correctly maps into a set of objects

  Scenario: Single, nested schema
    Given a valid nested schema definition
    When it is fully defined
    Then it correctly maps into a set of objects
