package jmri.jmrix.roco.z21;

import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetListenerScaffold;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Z21XNetInitializationManagerTest.java

 Description:	tests for the jmri.jmrix.roco.z21.Z21XNetInitializationManager
 class

 Description:	tests for the jmri.jmrix.roco.z21.z21XNetInitializationManager
 class
 *
 * @author	Paul Bender Copyright (C) 2015
 * @version $Revision: 22710 $
 */
public class Z21XNetInitializationManagerTest extends TestCase {

    public void testCtor() {

// infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
        XNetListenerScaffold l = new XNetListenerScaffold();

        XNetSystemConnectionMemo memo = new XNetSystemConnectionMemo(t);

        Z21XNetInitializationManager m = new Z21XNetInitializationManager(memo) {
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
    public Z21XNetInitializationManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", Z21XNetInitializationManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(Z21XNetInitializationManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

}
