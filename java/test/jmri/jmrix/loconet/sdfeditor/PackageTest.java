package jmri.jmrix.loconet.sdfeditor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.loconet.sdfeditor package.
 *
 * @author	Bob Jacobsen Copyright 2007
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    MonitoringLabelTest.class,
    EditorPaneTest.class,
    BranchToEditorTest.class,
    ChannelStartEditorTest.class,
    CommentMacroEditorTest.class,
    DelaySoundEditorTest.class,
    EndSoundEditorTest.class,
    FourByteMacroEditorTest.class,
    TwoByteMacroEditorTest.class,
    GenerateTriggerEditorTest.class,
    InitiateSoundEditorTest.class,
    LabelMacroEditorTest.class,
    LoadModifierEditorTest.class,
    MaskCompareEditorTest.class,
    PlayEditorTest.class,
    SdlVersionEditorTest.class,
    SkemeStartEditorTest.class,
    SkipOnTriggerEditorTest.class,
    EditorFrameTest.class,
    BundleTest.class
})
public class PackageTest {
}
