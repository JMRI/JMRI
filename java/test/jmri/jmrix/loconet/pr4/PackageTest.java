package jmri.jmrix.loconet.pr4;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        jmri.jmrix.loconet.pr4.configurexml.PackageTest.class,
        jmri.jmrix.loconet.pr4.swing.PackageTest.class,
        PR4AdapterTest.class,
        PR4SystemConnectionMemoTest.class,
})

/**
 * Tests for the jmri.jmrix.loconet.pr4 package.
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2006, 2008
 */
public class PackageTest  {
}
