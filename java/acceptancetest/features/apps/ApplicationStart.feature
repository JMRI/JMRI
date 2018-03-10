@apptest @headed
Feature: JMRI Applications 

Scenario Outline: Application Start
   Given I am using profile <profile>
   When starting application <application> with <name>
   Then <infoline> is printed to the console
   And a frame with title <name> is displayed

   @FailingTests @Ignore
   Examples: Tests that are failing
   | application | profile | name | infoline |
   | apps.PanelPro.PanelPro | java/test/apps/PanelPro/profiles/EasyDcc_Simulator | PanelProPro | Main initialization done |
   | apps.PanelPro.PanelPro | java/test/apps/PanelPro/profiles/Sprog_Simulator | PanelPro | Main initialization done |
   | apps.DecoderPro.DecoderPro | java/test/apps/PanelPro/profiles/Sprog_Simulator | DecoderPro | DecoderPro version |
   | apps.SoundPro.SoundPro | java/test/apps/PanelPro/profiles/Sprog_Simulator | SoundPro | SoundPro version |
   | apps.DispatcherPro.DispatcherPro | java/test/apps/PanelPro/profiles/Sprog_Simulator | DispatcherPro | DispatcherPro version |

   @PanelProTest 
   Examples: PanelPro Tests
   | application | profile | name | infoline |
   | apps.PanelPro.PanelPro | java/test/apps/PanelPro/profiles/LocoNet_Simulator | PanelPro | Main initialization done |
   | apps.PanelPro.PanelPro | java/test/apps/PanelPro/profiles/TMCC_Simulator | PanelPro | Main initialization done |
   | apps.PanelPro.PanelPro | java/test/apps/PanelPro/profiles/Prevent_Init_Loop | PanelPro | Main initialization done |
   | apps.PanelPro.PanelPro | java/test/apps/PanelPro/profiles/Grapevine_Simulator | PanelPro | Main initialization done |

   @DecoderProTest
   Examples: DecoderPro Tests
   | application | profile | name | infoline |
   | apps.DecoderPro.DecoderPro | java/test/apps/PanelPro/profiles/LocoNet_Simulator | DecoderPro | DecoderPro version |
   | apps.DecoderPro.DecoderPro | java/test/apps/PanelPro/profiles/EasyDcc_Simulator | DecoderPro | DecoderPro version |
   | apps.DecoderPro.DecoderPro | java/test/apps/PanelPro/profiles/TMCC_Simulator | DecoderPro | DecoderPro version |
   | apps.DecoderPro.DecoderPro | java/test/apps/PanelPro/profiles/Prevent_Init_Loop | DecoderPro | DecoderPro version |
   | apps.DispatcherPro.DispatcherPro | java/test/apps/PanelPro/profiles/Grapevine_Simulator | DispatcherPro | DispatcherPro version |

   @SoundProTest
   Examples: SoundPro Tests
   | application | profile | name | infoline |
   | apps.SoundPro.SoundPro | java/test/apps/PanelPro/profiles/LocoNet_Simulator | SoundPro | SoundPro version |
   | apps.SoundPro.SoundPro | java/test/apps/PanelPro/profiles/EasyDcc_Simulator | SoundPro | SoundPro version |
   | apps.SoundPro.SoundPro | java/test/apps/PanelPro/profiles/TMCC_Simulator | SoundPro | SoundPro version |
   | apps.SoundPro.SoundPro | java/test/apps/PanelPro/profiles/Prevent_Init_Loop | SoundPro | SoundPro version |
   | apps.SoundPro.SoundPro | java/test/apps/PanelPro/profiles/Grapevine_Simulator | SoundPro | SoundPro version |

   @DispatcherProTest
   Examples: DispatcherPro Tests
   | application | profile | name | infoline |
   | apps.DispatcherPro.DispatcherPro | java/test/apps/PanelPro/profiles/LocoNet_Simulator | DispatcherPro | DispatcherPro version |
   | apps.DispatcherPro.DispatcherPro | java/test/apps/PanelPro/profiles/EasyDcc_Simulator | DispatcherPro | DispatcherPro version |
   | apps.DispatcherPro.DispatcherPro | java/test/apps/PanelPro/profiles/TMCC_Simulator | DispatcherPro | DispatcherPro version |
   | apps.DispatcherPro.DispatcherPro | java/test/apps/PanelPro/profiles/Prevent_Init_Loop | DispatcherPro | DispatcherPro version |
   | apps.DispatcherPro.DispatcherPro | java/test/apps/PanelPro/profiles/Grapevine_Simulator | DispatcherPro | DispatcherPro version |
