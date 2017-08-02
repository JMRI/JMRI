package jmri.web.servlet.operations;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    OperationsServletTest.class,
    HtmlConductorTest.class,
    HtmlTrainCommonTest.class,
    HtmlManifestTest.class
})

/**
 * Invokes complete set of tests in the jmri.web.servlet.operations tree
 *
 * @author	Bob Jacobsen Copyright 2008
 * @author	Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
