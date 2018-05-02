package jmri.jmrit.sound;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Invokes complete set of tests in the jmri.jmrit.sound tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003
 * @author Randall Wood (C) 2016
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    SoundUtilTest.class,
    WavBufferTest.class
})
public class PackageTest {
}
