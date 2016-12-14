package jmri.web.servlet.panel;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    ControlPanelServletTest.class,
    LayoutPanelServletTest.class,
    PanelServletTest.class
})
/**
 * Invokes complete set of tests in the jmri.web.servlet.panel tree
 *
 * @author	Bob Jacobsen Copyright 2008
 * @author	Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
