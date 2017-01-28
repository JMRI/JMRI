package jmri.jmrit.audio;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   BundleTest.class,
   jmri.jmrit.audio.swing.PackageTest.class,
   jmri.jmrit.audio.configurexml.PackageTest.class,
   JoalAudioListenerTest.class,
   JoalAudioBufferTest.class,
   NullAudioListenerTest.class,
   NullAudioBufferTest.class,
   JavaSoundAudioListenerTest.class,
   JavaSoundAudioBufferTest.class
})
/**
 * Invokes complete set of tests in the jmri.jmrit.audio tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 * @author  Paul Bender Copyright (C) 2017
 */
public class PackageTest {
}
