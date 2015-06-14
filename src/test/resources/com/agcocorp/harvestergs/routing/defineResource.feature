Feature: Define a resource
  Scenario: Single valid resource
      Given a valid resource definition
       When I get its full specification
       Then I get a complete schema
        And list of paths
