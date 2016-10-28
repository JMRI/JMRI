package jmri.server.json.turnout;

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
    JsonTurnoutHttpServiceTest.class,
    JsonTurnoutSocketServiceTest.class
})
public class PackageTest {
}
