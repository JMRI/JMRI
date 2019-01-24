package jmri.jmrix.srcp.networkdriver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.srcp.networkdriver.configurexml.PackageTest.class,
   NetworkDriverAdapterTest.class
})
/**
 * Tests for the jmri.jmrix.srcp.networkdriver package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
