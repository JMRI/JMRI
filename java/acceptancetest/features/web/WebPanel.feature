#These tests load a panel window, so must run headed.
@webtest @Headed
Feature: JMRI Web Panel 

Scenario Outline: Web Panel requests
   Given I am using <browser>
   And panel <panel> is loaded
   When I ask for the url <panelURL>
   Then a page with title <PageTitle> is returned

   @firefox
   Examples: Firefox Panel Tests
   | browser | panel | panelURL | PageTitle | 
   | firefox | java/test/jmri/jmrit/cabsignals/SimpleCabSignalTestPanel.xml | http://localhost:12080/panel/Layout/Cab%20Signal%20Test  | Layout/Cab%20Signal%20Test \| My JMRI Railroad | 

   @chrome
   Examples: Chrome Panel Tests
   | browser | panel | panelURL | PageTitle |
   | chrome | java/test/jmri/jmrit/cabsignals/SimpleCabSignalTestPanel.xml | http://localhost:12080/panel/Layout/Cab%20Signal%20Test  | Layout/Cab%20Signal%20Test \| My JMRI Railroad | 

