package jmri.managers;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    DefaultConditionalManagerTest.class,
    DefaultIdTagManagerTest.class,
    DefaultLogixManagerTest.class,
    LogixSystemTest.class,
    DefaultSignalSystemManagerTest.class,
    ProxyLightManagerTest.class,
    ProxySensorManagerTest.class,
    ProxyTurnoutManagerTest.class,
    JmriUserPreferencesManagerTest.class,
    BundleTest.class,
    jmri.managers.configurexml.PackageTest.class,
    ProxyReporterManagerTest.class,
    ManagerDefaultSelectorTest.class,
    AbstractSignalHeadManagerTest.class,
    DefaultInstanceInitializerTest.class,
    DefaultMemoryManagerTest.class,
    DefaultPowerManagerTest.class,
    DefaultProgrammerManagerTest.class,
    DefaultRailComManagerTest.class,
    DefaultRouteManagerTest.class,
    DefaultShutDownManagerTest.class,
    DefaultSignalGroupManagerTest.class,
    DefaultSignalMastLogicManagerTest.class,
    DefaultSignalMastManagerTest.class,
    DeferringProgrammerManagerTest.class
})

/**
 * Invoke complete set of tests for the jmri.managers
 *
 * @author	Bob Jacobsen, Copyright (C) 2009
 */
public class PackageTest {

}
