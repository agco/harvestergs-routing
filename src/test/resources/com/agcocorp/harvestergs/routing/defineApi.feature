Feature: Define an API
  Scenario: Complete API
    Given a complete API definition
    When I get its resources and attributes
    Then I get a complete, correct list

  Scenario: Builder-based definition
     Given a set of resource builders
      When I define an API using them
      Then I get a correct list, with all builder results
