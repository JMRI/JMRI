package jmri.jmrix.dccpp.dccppovertcp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   jmri.jmrix.dccpp.dccppovertcp.configurexml.PackageTest.class,
   DCCppOverTcpPacketizerTest.class,
   ServerTest.class,
   ServerActionTest.class,
   ServerFrameTest.class,
   ConnectionConfigTest.class,
   DCCppTcpDriverAdapterTest.class,
   ClientRxHandlerTest.class,
   BundleTest.class
})
/**
 * Tests for the jmri.jmrix.dccpp.dccppovertcp package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
