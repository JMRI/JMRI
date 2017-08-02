package jmri.jmrit.audio.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   BundleTest.class,
   AudioBufferFrameTest.class,
   AudioListenerFrameTest.class,
   AudioSourceFrameTest.class
})

/**
 * Invokes complete set of tests in the jmri.jmrit.audio.swing tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
public class PackageTest {
}
