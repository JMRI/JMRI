package jmri.jmrix.loconet.pr3;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        jmri.jmrix.loconet.pr3.configurexml.PackageTest.class,
        jmri.jmrix.loconet.pr3.swing.PackageTest.class,
        PR3AdapterTest.class,
        PR3SystemConnectionMemoTest.class,
})

/**
 * Tests for the jmri.jmrix.loconet.pr3 package.
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2006, 2008
 */
public class PackageTest  {
}
