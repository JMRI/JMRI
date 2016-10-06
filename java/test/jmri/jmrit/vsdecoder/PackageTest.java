package jmri.jmrit.vsdecoder;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    VSDecoderBundleTest.class,
    jmri.jmrit.vsdecoder.swing.PackageTest.class
})

/**
 * Tests for the jmri.jmrit.vsdecoder package.
 *
 * @author	Mark Underwood Copyright (C) 2011
 */
public class PackageTest {
}
