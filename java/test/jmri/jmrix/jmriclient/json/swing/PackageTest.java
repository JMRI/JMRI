package jmri.jmrix.jmriclient.json.swing;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
