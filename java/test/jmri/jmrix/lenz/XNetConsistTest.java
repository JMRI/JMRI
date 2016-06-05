package jmri.jmrix.lenz;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XNetConsistTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetConsist class
 *
 * @author	Paul Bender
 */
public class XNetConsistTest {

    private XNetInterfaceScaffold tc = null;
    private XNetSystemConnectionMemo memo = null;

    @Test public void integerConstructorTest() {
        // infrastructure objects

        XNetConsist c = new XNetConsist(5, tc, memo);
        Assert.assertNotNull(c);
    }

    @Test public void dccLocoAddressConstructorTest() {
        // infrastructure objects

        jmri.DccLocoAddress addr = new jmri.DccLocoAddress(5,false);

        XNetConsist c = new XNetConsist(addr, tc, memo);
        Assert.assertNotNull(c);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        tc = new XNetInterfaceScaffold(new LenzCommandStation());
        memo = new XNetSystemConnectionMemo(tc);
    }
   
    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        tc=null;
        memo=null;
    }

}
