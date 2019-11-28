package jmri.server.json.signalmast;

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
    JsonSignalMastTest.class
})
public class PackageTest {
}
