package jmri.util.com;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   jmri.util.com.rbnb.PackageTest.class,
   jmri.util.com.sun.PackageTest.class,
   jmri.util.com.dictiography.collections.PackageTest.class
})
/**
 * Tests for the jmri.util.com package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
