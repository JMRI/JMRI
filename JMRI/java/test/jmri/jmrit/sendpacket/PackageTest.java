package jmri.jmrit.sendpacket;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   SendPacketActionTest.class,
   SendPacketFrameTest.class,
   SendPacketTest.class,
   BundleTest.class
})

/**
 * Invokes complete set of tests in the jmri.jmrit.sendpacket tree
 *
 * @author	Paul Bender Copyright (C) 2015,2016
 */
public class PackageTest {
}
