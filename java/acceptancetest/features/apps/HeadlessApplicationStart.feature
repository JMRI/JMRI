@apptest @headless
Feature: Headless JMRI Applications 

Scenario Outline: Application Start
   Given I am using profile <profile>
   When starting application <application> with <name> 
   Then <infoline> is printed to the console

   @JmriFacelessTest @Ignore
   Examples: Headless Tests
   | application | profile | name | infoline |
   | apps.JmriFaceless | java/test/apps/PanelPro/profiles/LocoNet_Simulator | JmriFaceless | JmriFaceless version |
   | apps.JmriFaceless | java/test/apps/PanelPro/profiles/EasyDcc_Simulator | JmriFaceless | JmriFaceless version |
   | apps.JmriFaceless | java/test/apps/PanelPro/profiles/TMCC_Simulator | JmriFaceless | JmriFaceless version |
   | apps.JmriFaceless | java/test/apps/PanelPro/profiles/Grapevine_Simulator | JmriFaceless | JmriFaceless version |
