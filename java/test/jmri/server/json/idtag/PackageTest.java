package jmri.server.json.idtag;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 *
 * @author Randall Wood (C) 2019
 */
@RunWith(Suite.class)
@SuiteClasses({
    BundleTest.class,
    JsonIdTagHttpServiceTest.class,
    JsonIdTagSocketServiceTest.class,
    JsonIdTagTest.class
})
public class PackageTest {
}
