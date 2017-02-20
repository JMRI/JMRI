package jmri.web.servlet.json;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    JsonServletTest.class
})

/**
 * Invokes complete set of tests in the jmri.web.servlet.json tree
 *
 * @author	Bob Jacobsen Copyright 2008
 * @author	Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
