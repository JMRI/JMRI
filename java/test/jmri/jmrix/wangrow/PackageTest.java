package jmri.jmrix.wangrow;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   jmri.jmrix.wangrow.serialdriver.PackageTest.class,
   WangrowConnectionTypeListTest.class,
   WangrowMenuTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.wangrow package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
