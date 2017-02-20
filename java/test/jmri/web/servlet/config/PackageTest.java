package jmri.web.servlet.config;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    ConfigServletTest.class
})

/**
 * Invokes complete set of tests in the jmri.web.servlet.config tree
 *
 * @author	Bob Jacobsen Copyright 2008
 * @author	Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
