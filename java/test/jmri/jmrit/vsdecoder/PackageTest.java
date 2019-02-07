package jmri.jmrit.vsdecoder;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    TriggerTest.class,
    BoolTriggerTest.class,
    ButtonTriggerTest.class,
    FloatTriggerTest.class,
    IntTriggerTest.class,
    NotchTriggerTest.class,
    VSDSoundTest.class,
    SoundBiteTest.class,
    BundleTest.class,
    jmri.jmrit.vsdecoder.swing.PackageTest.class,
    jmri.jmrit.vsdecoder.listener.PackageTest.class,
    AudioUtilTest.class,
    DieselPaneTest.class,
    EnginePaneTest.class,
    EngineSoundEventTest.class,
    LoadVSDFileActionTest.class,
    LoadXmlVSDecoderActionTest.class,
    MomentarySoundEventTest.class,
    SoundEventTest.class,
    StoreXmlVSDecoderActionTest.class,
    ToggleSoundEventTest.class,
    VSDConfigPanelTest.class,
    VSDConfigTest.class,
    VSDOptionPanelTest.class,
    VSDSoundsPanelTest.class,
    VSDecoderCreationActionTest.class,
    VSDecoderFrameTest.class,
    VSDecoderManagerTest.class,
    VSDecoderPreferencesTest.class,
    VSDecoderManagerThreadTest.class,
    VSDecoderTest.class,
    VSDManagerEventTest.class,
    VSDecoderEventTest.class,
    VSDecoderCreationStartupActionFactoryTest.class,
    ConfigurableSoundTest.class,
    DieselSoundTest.class,
    Diesel2SoundTest.class,
    Diesel3SoundTest.class,
    EngineSoundTest.class,
    SteamSoundTest.class,
    Steam1SoundTest.class,
    ThrottleTriggerTest.class,
    NotchSoundTest.class,
    NotchTransitionTest.class,
    VSDFileTest.class,
    VSDecoderPaneTest.class,
    VSDGeoFileTest.class,
})

/**
 * Tests for the jmri.jmrit.vsdecoder package.
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class PackageTest {
}
