package jmri.jmrix.jinput;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   TreeModelTest.class,
   jmri.jmrix.jinput.treecontrol.PackageTest.class,
   UsbNodeTest.class
})
/**
 * Tests for the jmri.jmrix.jinput package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
