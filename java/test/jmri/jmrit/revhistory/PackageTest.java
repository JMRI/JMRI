package jmri.jmrit.revhistory;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   FileHistoryTest.class,
   jmri.jmrit.revhistory.configurexml.PackageTest.class,
   jmri.jmrit.revhistory.swing.PackageTest.class
})
/**
 * Tests for the jmri.jmrit.revhistory package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
