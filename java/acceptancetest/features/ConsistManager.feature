Feature: InstanceManager

Background:
   Given the InstanceManager is started

Scenario: Consist Manager is null
   When I ask for the Consist Manager
   Then the consist manager is null

Scenario: NMRA Consist Manager installed with Command Station instance
   Given A Command Station Instance
   When I ask for the Consist Manager
   Then the consist manager is not null
   And the consist manager is an Nmra Consist Manager

Scenario: Dcc Consist Manager installed with an Operations Mode Programmer instance
   Given An Operations Mode Programmer Instance
   When I ask for the Consist Manager
   Then the consist manager is not null
   And the consist manager is an Dcc Consist Manager
