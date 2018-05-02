package jmri.web.server;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    WebServerTest.class,
    FrameServletPreferencesPanelTest.class,
    RailroadNamePreferencesPanelTest.class,
    WebServerActionTest.class,
    WebServerPreferencesInstanceInitializerTest.class,
    WebServerPreferencesPanelTest.class,
    WebServerPreferencesTest.class
})

/**
 * Invokes complete set of tests in the jmri.web.server tree
 *
 * @author	Bob Jacobsen Copyright 2008
 * @author	Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
