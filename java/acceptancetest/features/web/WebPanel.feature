#These tests load a panel window, so must run headed.
@webtest @webpanel @Headed
Feature: JMRI Web Panel

Scenario Outline: Web Panel requests
   Given I am using <browser>
   And panel <panel> is loaded
   When I ask for the url <panelURL>
   Then either <PageTitle> or <FormattedPageTitle> is returned as the title

   # firefox version of the test appears to be hanging on travis
   @firefox @Ignore
   Examples: Firefox Panel Tests
   | browser | panel | panelURL | PageTitle | FormattedPageTitle |
   | firefox | java/test/jmri/jmrit/cabsignals/SimpleCabSignalTestPanel.xml | http://localhost:12080/panel/Layout/Cab%20Signal%20Test  | Layout/Cab%20Signal%20Test \| My JMRI Railroad |Cab Signal Test \| My JMRI Railroad|
   | firefox | java/test/jmri/jmrit/display/layoutEditor/load/LayoutEditorTest.xml | http://localhost:12080/panel/Layout/Layout%20Editor%20Test  | Layout/Layout%20Editor%20Test \| My JMRI Railroad | Layout Editor Test \| My JMRI Railroad |
   | firefox | java/test/jmri/jmrit/display/layoutEditor/load/Decorations.xml | http://localhost:12080/panel/Layout/Decorations%20Testing  | Layout/Decorations%20Testing \| My JMRI Railroad | Decorations Testing \| My JMRI Railroad |

  @chrome
   Examples: Chrome Panel Tests
   | browser | panel | panelURL | PageTitle | FormattedPageTitle |
   | chrome | java/test/jmri/jmrit/cabsignals/SimpleCabSignalTestPanel.xml | http://localhost:12080/panel/Layout/Cab%20Signal%20Test  | Layout/Cab%20Signal%20Test \| My JMRI Railroad |Cab Signal Test \| My JMRI Railroad|
   | chrome | java/test/jmri/jmrit/display/layoutEditor/load/LayoutEditorTest.xml | http://localhost:12080/panel/Layout/Layout%20Editor%20Test  | Layout/Layout%20Editor%20Test \| My JMRI Railroad | Layout Editor Test \| My JMRI Railroad |
   | chrome | java/test/jmri/jmrit/display/layoutEditor/load/Decorations.xml | http://localhost:12080/panel/Layout/Decorations%20Testing  | Layout/Decorations%20Testing \| My JMRI Railroad | Decorations Testing \| My JMRI Railroad |

