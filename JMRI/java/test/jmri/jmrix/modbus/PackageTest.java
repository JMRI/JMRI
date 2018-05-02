package jmri.jmrix.modbus;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        jmri.jmrix.modbus.common.PackageTest.class,
        jmri.jmrix.modbus.slave.PackageTest.class,
        jmri.jmrix.modbus.master.PackageTest.class,
})

/**
 * Tests for the jmri.jmrix.modbus package.
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2014
 */
public class PackageTest  {
}
