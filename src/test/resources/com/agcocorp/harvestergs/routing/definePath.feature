Feature: Define a path
  Scenario: Single valid path
    Given a valid path definition
    When I request its expanded list of paths
    Then I get a correct list of paths and handlers