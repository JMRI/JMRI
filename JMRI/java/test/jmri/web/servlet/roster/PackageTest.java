package jmri.web.servlet.roster;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    RosterServletTest.class,
    FileMetaTest.class,
    MultipartRequestHandlerTest.class
})
/**
 * Invokes complete set of tests in the jmri.web.servlet.roster tree
 *
 * @author	Bob Jacobsen Copyright 2008
 * @author	Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
