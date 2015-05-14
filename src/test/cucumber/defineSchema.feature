Feature: Define a schema
  Scenario: Single valid schema
    Given a valid schema definition
    When the schema is fully defined
    Then it correctly maps into a set of objects