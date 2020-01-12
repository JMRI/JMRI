package jmri.web.servlet;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   jmri.web.servlet.config.PackageTest.class,
   jmri.web.servlet.directory.PackageTest.class,
   jmri.web.servlet.frameimage.PackageTest.class,
   jmri.web.servlet.home.PackageTest.class,
   jmri.web.servlet.json.PackageTest.class,
   jmri.web.servlet.operations.PackageTest.class,
   jmri.web.servlet.panel.PackageTest.class,
   jmri.web.servlet.roster.PackageTest.class,
   jmri.web.servlet.simple.PackageTest.class,
   jmri.web.servlet.about.PackageTest.class,
   jmri.web.servlet.tables.PackageTest.class,
   BundleTest.class,
   DenialServletTest.class,
   RedirectionServletTest.class,
   ServletUtilTest.class
})

/**
 * Invokes complete set of tests in the jmri.web.servlet tree
 *
 * @author	Bob Jacobsen Copyright 2013
 * @author	Paul Bender Copyright (C) 2016
 */
public class PackageTest {

}
