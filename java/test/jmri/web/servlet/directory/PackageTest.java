package jmri.web.servlet.directory;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    DirectoryHandlerTest.class,
    DirectoryResourceTest.class,
    DirectoryServiceTest.class
})

/**
 * Invokes complete set of tests in the jmri.web.servlet.directory tree
 *
 * @author	Bob Jacobsen Copyright 2008
 * @author	Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
