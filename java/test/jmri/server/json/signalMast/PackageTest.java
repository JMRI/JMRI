package jmri.server.json.signalMast;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 *
 * @author Randall Wood (C) 2016
 */
@RunWith(Suite.class)
@SuiteClasses({
    BundleTest.class,
    JsonSignalMastHttpServiceTest.class,
    JsonSignalMastSocketServiceTest.class,
    JsonSignalMastServiceFactoryTest.class
})
public class PackageTest {
}
