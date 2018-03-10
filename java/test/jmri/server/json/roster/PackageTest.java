package jmri.server.json.roster;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Randall Wood (C) 2016
 */
@RunWith(Suite.class)
@SuiteClasses({
    BundleTest.class,
    JsonRosterHttpServiceTest.class,
    JsonRosterSocketServiceTest.class,
    JsonRosterTest.class
})
public class PackageTest {
}
