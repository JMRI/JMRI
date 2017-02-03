package jmri.web;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   jmri.web.servlet.PackageTest.class,
   jmri.web.server.PackageTest.class,
   BundleTest.class
})

/**
 * Invokes complete set of tests in the jmri.web tree
 *
 * @author	Bob Jacobsen Copyright 2008
 * @author	Paul Bender Copyright (C) 2016
 */
public class PackageTest {

}
