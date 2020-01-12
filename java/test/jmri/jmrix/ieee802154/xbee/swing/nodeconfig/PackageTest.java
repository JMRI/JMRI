package jmri.jmrix.ieee802154.xbee.swing.nodeconfig;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    XBeeNodeConfigActionTest.class,
    XBeeNodeConfigFrameTest.class,
    XBeeAddNodeFrameTest.class,
    XBeeEditNodeFrameTest.class,
    AssignmentTableModelTest.class,
    StreamConfigPaneTest.class
})

/**
 * Tests for the jmri.jmrix.ieee802154.xbee.swing.nodeconfig package
 *
 * @author  Paul Bender	Copyright (C) 2016
 */
public class PackageTest{
}
