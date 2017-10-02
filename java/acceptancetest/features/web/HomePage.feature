@webtest
Feature: JMRI Home Page

Scenario: Http request for /index.html
   When I ask for the /index.html
   Then the home page is returned

