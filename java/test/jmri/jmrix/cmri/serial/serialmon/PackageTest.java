package jmri.jmrix.cmri.serial.serialmon;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    SerialMonActionTest.class,
    SerialMonFrameTest.class,
    BundleTest.class,
    SerialFilterFrameTest.class,
    SerialFilterActionTest.class,
})

/**
 * Tests for the jmri.jmrix.cmri.serial.serialmon package
 *
 * @author  Paul Bender	Copyright (C) 2016
 */
public class PackageTest{
}
