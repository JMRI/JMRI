package jmri.jmrix.ztc.ztc611;

import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetListenerScaffold;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * ZTC611XNetInitializationManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.ztc.ztc611.ZTC611XNetInitializationManager
 * class
 *
 * @author	Paul Bender
 */
public class ZTC611XNetInitializationManagerTest extends TestCase {

    public void testCtor() {

// infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
        XNetListenerScaffold l = new XNetListenerScaffold();

        XNetSystemConnectionMemo memo = new XNetSystemConnectionMemo(t);

        ZTC611XNetInitializationManager m = new ZTC611XNetInitializationManager(memo) {
            @Override
            protected int getInitTimeout() {
                return 50;   // shorten, because this will fail & delay test
            }
        };
        Assert.assertNotNull("exists", t);
        Assert.assertNotNull("exists", l);
        Assert.assertNotNull("exists", m);
        Assert.assertNotNull("exists", memo);
    }

    // from here down is testing infrastructure
    public ZTC611XNetInitializationManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ZTC611XNetInitializationManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ZTC611XNetInitializationManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

}
