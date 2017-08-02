Feature: InstanceManager

Background:
   Given the InstanceManager is started

Scenario: Consist Manager is null
   When I ask for the Consist Manager
   Then the consist manager is null
