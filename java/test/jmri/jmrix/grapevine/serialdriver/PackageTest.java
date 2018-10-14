package jmri.jmrix.grapevine.serialdriver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   SerialDriverAdapterTest.class,
   jmri.jmrix.grapevine.serialdriver.configurexml.PackageTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.grapevine.serialdriver package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {

}
