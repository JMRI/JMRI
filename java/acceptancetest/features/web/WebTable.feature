@webtest @webtable
Feature: JMRI Web Table

Scenario Outline: Web Table requests
   Given I am using <browser>
   When panel <panel> is loaded
   And  <table> is visible
   Then item <item> with entry <column> has state <state>

   @firefox
   Examples: Firefox Table Tests
   | browser | panel | table | item | column | state |
   | firefox | java/test/jmri/jmrit/display/configurexml/load/OneOfEach.xml | Turnouts | IT0 | state |closed |
   | firefox | java/test/jmri/jmrit/display/configurexml/load/OneOfEach.xml | Turnouts | IT1 | state |unknown |

   @chrome
   Examples: Chrome TableTests
   | browser | panel | table | item | column | state |
   | chrome| java/test/jmri/jmrit/display/configurexml/load/OneOfEach.xml | Turnouts | IT0 | state |closed |
   | chrome | java/test/jmri/jmrit/display/configurexml/load/OneOfEach.xml | Turnouts | IT1 | state |unknown |

