package jmri.web.servlet.simple;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    SimpleServletTest.class
})

/**
 * Invokes complete set of tests in the jmri.web.servlet.simple tree
 *
 * @author	Bob Jacobsen Copyright 2008
 * @author	Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
