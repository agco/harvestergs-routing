Feature: Define a schema
  Scenario: Complete schema
      Given a complete schema definition
       When I get its corresponding JSON schema
       Then the schema correctly maps all definitions
        And is a JSON schema compliant
