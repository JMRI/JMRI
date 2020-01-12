package jmri.jmrix.cmri.serial.nodeiolist;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.cmri.serial.nodeiolist package.
 *
 * @author Bob Jacobsen Copyright 2003, 2017
 * @author Paul Bender Copyright (C) 2016
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   NodeIOListFrameTest.class,
   BundleTest.class,
   NodeIOListActionTest.class,
})

public class PackageTest{
}
