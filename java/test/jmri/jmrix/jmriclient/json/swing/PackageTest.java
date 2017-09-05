package jmri.jmrix.jmriclient.json.swing;

import junit.framework.TestCase;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   BundleTest.class,
   JsonPacketGenFrameTest.class,
   JsonClientComponentFactoryTest.class,
   JsonClientMenuTest.class,
   JsonClientMonitorActionTest.class,
   JsonClientMonitorFrameTest.class,
   JsonPacketGenActionTest.class
})
/**
 * Tests for the jmri.jmrix.jmriclient package
 *
 * @author	Bob Jacobsen
 * @author  Paul Bender Copyright (C) 2017	
 */
public class PackageTest extends TestCase {


}
