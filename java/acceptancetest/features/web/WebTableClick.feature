@webtest @webtable
Feature: JMRI Web Table Click

Scenario Outline: Web Table requests with click testing
   Given I am using <browser>
   When panel <panel> is loaded
   Then table <table> has row <row> column <column> with text <text> after click <after>

   @firefox 
   Examples: Firefox Table Click Tests
   | browser | panel | table | row | column | text | after |
   | firefox | java/test/jmri/jmrit/display/configurexml/load/OneOfEach.xml | Turnouts | IT0 | state | closed | thrown | 
   | firefox | java/test/jmri/jmrit/display/configurexml/load/OneOfEach.xml | Turnouts | IT1 | state | unknown | thrown |
   | firefox | java/test/jmri/jmrit/display/configurexml/load/OneOfEach.xml | Lights | IL0 | state | off | on |

   @chrome
   Examples: Chrome Table Click Tests
   | browser | panel | table | row | column | text | after |
   | chrome | java/test/jmri/jmrit/display/configurexml/load/OneOfEach.xml | Turnouts | IT2 | state | closed | thrown | 
   | chrome | java/test/jmri/jmrit/display/configurexml/load/OneOfEach.xml | Turnouts | IT4 | inverted | false | false |
   | chrome | java/test/jmri/jmrit/display/configurexml/load/OneOfEach.xml | Sensors | ISCLOCKRUNNING | state | active | inactive | 
   | chrome | java/test/jmri/jmrit/display/configurexml/load/OneOfEach.xml | Memories | IMRATEFACTOR | value | 1.0 | 1.0 | 
   | chrome | java/test/jmri/jmrit/display/configurexml/load/OneOfEach.xml | Signal Heads | IH8 | lit | true | true | 
   