package jmri.jmrix.loconet.loconetovertcp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.loconet.loconetovertcp.configurexml.PackageTest.class,
   LnOverTcpPacketizerTest.class,
<<<<<<< HEAD
   ServerTest.class,
   ServerActionTest.class,
   ServerFrameTest.class,
   LnTcpDriverAdapterTest.class
=======
   LnTcpServerTest.class,
   LnTcpServerActionTest.class,
   LnTcpServerFrameTest.class,
   LnTcpDriverAdapterTest.class,
   ClientRxHandlerTest.class,
   BundleTest.class
>>>>>>> JMRI/master
})
/**
 * Tests for the jmri.jmrix.loconet.loconetovertcp package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
