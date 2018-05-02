package jmri.jmrix.dcc4pc.serialdriver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   SerialDriverAdapterTest.class,
   jmri.jmrix.dcc4pc.serialdriver.configurexml.PackageTest.class
})
/**
 * Tests for the jmri.jmrix.dcc4pc.serialdriver package
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
