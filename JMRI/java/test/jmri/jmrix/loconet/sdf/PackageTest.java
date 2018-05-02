package jmri.jmrix.loconet.sdf;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        InitiateSoundTest.class,
        PlayTest.class,
        SdfBufferTest.class,
        BranchToTest.class,
        ChannelStartTest.class,
        CommentMacroTest.class,
        DelaySoundTest.class,
        EndSoundTest.class,
        FourByteMacroTest.class,
        GenerateTriggerTest.class,
        LabelMacroTest.class,
        MaskCompareTest.class,
        LoadModifierTest.class,
        SdlVersionTest.class,
        SkemeStartTest.class,
        SkipOnTriggerTest.class,
        TwoByteMacroTest.class,
})

/**
 * Tests for the jmri.jmrix.loconet.sdf package.
 *
 * @author	Bob Jacobsen Copyright 2007
 */
public class PackageTest  {
}
