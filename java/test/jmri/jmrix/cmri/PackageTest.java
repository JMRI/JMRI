package jmri.jmrix.cmri;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   CMRISystemConnectionMemoTest.class,
   CMRIMenuTest.class,
   jmri.jmrix.cmri.serial.PackageTest.class,
   jmri.jmrix.cmri.swing.PackageTest.class,
   CMRIConnectionTypeListTest.class,
   BundleTest.class
})

/**
 * Tests for the jmri.jmrix.cmri package
 *
 * @author  Paul Bender	Copyright (C) 2016
 */
public class PackageTest{
}
