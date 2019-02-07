package jmri.jmrix.loconet.locormi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.loconet.locormi.configurexml.PackageTest.class,
   LnMessageBufferTest.class,
   LnMessageClientTest.class,
   LnMessageServerActionTest.class,
   LnMessageClientActionTest.class,
   LnMessageClientPollThreadTest.class,
   LnMessageServerTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.loconet.locormi package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
