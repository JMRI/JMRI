package jmri.jmrix.loconet.loconetovertcp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.loconet.loconetovertcp.configurexml.PackageTest.class,
   LnOverTcpPacketizerTest.class,
   LnTcpServerTest.class,
   LnTcpServerActionTest.class,
   LnTcpServerFrameTest.class,
   LnTcpDriverAdapterTest.class,
   ClientRxHandlerTest.class,
   BundleTest.class,
   LnTcpPreferencesPanelTest.class,
   LnTcpPreferencesTest.class,
   LnTcpStartupActionFactoryTest.class
})
/**
 * Tests for the jmri.jmrix.loconet.loconetovertcp package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
