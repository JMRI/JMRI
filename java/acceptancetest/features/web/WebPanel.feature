#These tests load a panel window, so JMRI must run headed, even though browsers are running headless
@webtest @webpanel @Headed
Feature: JMRI Web Panel

Scenario Outline: Web Panel requests
   Given I am using <browser>
   And panel <panel> is loaded
   When I ask for the url <panelURL>
   Then <PageTitle> is set as the title

   @firefox
   Examples: Firefox Panel Tests
   | browser | panel | panelURL | PageTitle |
   | firefox | java/test/jmri/jmrit/cabsignals/SimpleCabSignalTestPanel.xml | http://localhost:12080/panel/Layout/Cab%20Signal%20Test | Cab Signal Test \| My JMRI Railroad |
   | firefox | java/test/jmri/jmrit/display/layoutEditor/load/LayoutEditorTest-4-19-4.xml | http://localhost:12080/panel/Layout/Layout%20Editor%20Test | Layout Editor Test \| My JMRI Railroad |
   | firefox | java/test/jmri/jmrit/display/layoutEditor/load/Decorations-4-19-6.xml | http://localhost:12080/panel/Layout/Decorations%20Testing | Decorations Testing \| My JMRI Railroad |

   @chrome
   Examples: Chrome Panel Tests
   | browser | panel | panelURL | PageTitle |
   | chrome | java/test/jmri/jmrit/cabsignals/SimpleCabSignalTestPanel.xml | http://localhost:12080/panel/Layout/Cab%20Signal%20Test | Cab Signal Test \| My JMRI Railroad |
   | chrome | java/test/jmri/jmrit/display/layoutEditor/load/LayoutEditorTest-4-19-4.xml | http://localhost:12080/panel/Layout/Layout%20Editor%20Test | Layout Editor Test \| My JMRI Railroad |
   | chrome | java/test/jmri/jmrit/display/layoutEditor/load/Decorations-4-19-6.xml | http://localhost:12080/panel/Layout/Decorations%20Testing | Decorations Testing \| My JMRI Railroad |

