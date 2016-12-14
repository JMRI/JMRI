package jmri.jmrix.lenz.ztc640;

import org.junit.Assert;
import junit.framework.TestCase;

/**
 * <p>
 * Title: ZTC640XNetPacketizerTest </p>
 * <p>
 * Description: </p>
 * <p>
 * Copyright: Copyright (c) 2009</p>
 *
 * @author Paul Bender
 */
public class ZTC640XNetPacketizerTest extends TestCase {

    public void testCtor() {
        ZTC640XNetPacketizer f = new ZTC640XNetPacketizer(new jmri.jmrix.lenz.LenzCommandStation());
        Assert.assertNotNull(f);
    }

    // from here down is testing infrastructure
    public ZTC640XNetPacketizerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ZTC640XNetPacketizerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
