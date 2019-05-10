@webtest @webtable
Feature: JMRI Web Panel 

Scenario Outline: Web Panel requests
   Given I am using <browser>
   When panel <panel> is loaded
   Then <table> has item <item> with state <state>

   @firefox 
   Examples: Firefox Table Tests
   | browser | panel | table | item | state | 
   | firefox | java/test/jmri/jmrit/display/configurexml/load/OneOfEach.xml | Turnouts | IT0 | 2 | 
   | firefox | java/test/jmri/jmrit/display/configurexml/load/OneOfEach.xml | Turnouts | IT1 | 0 | 

   @chrome
   Examples: Chrome TableTests
   | browser | panel | table | item | state | 
   | chrome| java/test/jmri/jmrit/display/configurexml/load/OneOfEach.xml | Turnouts | IT0 | 2 | 
   | chrome | java/test/jmri/jmrit/display/configurexml/load/OneOfEach.xml | Turnouts | IT1 | 0 | 

