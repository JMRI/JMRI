package jmri.profile;

<<<<<<< HEAD
<<<<<<< HEAD
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invoke complete set of tests for the Jmri package
 *
 * @author	Bob Jacobsen, Copyright (C) 2001, 2002, 2007
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.profile.PackageTest");  // no tests in this class itself
        suite.addTest(new JUnit4TestAdapter(ProfileTest.class));
        suite.addTest(new TestSuite(ProfileUtilsTest.class));
        suite.addTest(new JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new JUnit4TestAdapter(ProfileListCellRendererTest.class));
        suite.addTest(new JUnit4TestAdapter(ProfileFileFilterTest.class));
        suite.addTest(new JUnit4TestAdapter(ProfileFileViewTest.class));
        suite.addTest(new JUnit4TestAdapter(ProfileListModelTest.class));
        suite.addTest(new JUnit4TestAdapter(ProfileManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(ProfilePreferencesPanelTest.class));
        suite.addTest(new JUnit4TestAdapter(ProfileTableModelTest.class));
        suite.addTest(new JUnit4TestAdapter(SearchPathTableModelTest.class));
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

=======
=======
>>>>>>> JMRI/master
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ProfileTest.class,
    ProfileUtilsTest.class,
    BundleTest.class,
    ProfileListCellRendererTest.class,
    ProfileFileFilterTest.class,
    ProfileFileViewTest.class,
    ProfileListModelTest.class,
    ProfileManagerTest.class,
    ProfilePreferencesPanelTest.class,
    ProfileTableModelTest.class,
<<<<<<< HEAD
    SearchPathTableModelTest.class
})
public class PackageTest {
>>>>>>> JMRI/master
=======
    SearchPathTableModelTest.class,
    ProfileConfigurationTest.class,
    ProfilePropertiesTest.class,
    NullProfileTest.class
})
public class PackageTest {
>>>>>>> JMRI/master
}
