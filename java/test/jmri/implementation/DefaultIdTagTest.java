// DefaultIdTagTest.java
package jmri.implementation;

import java.util.Calendar;
import java.util.Date;
import jmri.IdTag;
import jmri.Reporter;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the DefaultIdTag class
 *
 * @author Matthew Harris Copyright (C) 2011
 * @version $Revision$
 */
public class DefaultIdTagTest extends TestCase {

    public void testCreateIdTag() {
        IdTag r = new DefaultIdTag("ID0413276BC1");
        Assert.assertNotNull("IdTag not null", r);
    }

    public void testGetIdTagUserName() {
        IdTag r = new DefaultIdTag("ID0413276BC1", "Test Tag");
        Assert.assertEquals("IdTag user name is 'Test Tag'", "Test Tag", r.getUserName());
    }

    public void testGetIdTagTagID() {
        IdTag r = new DefaultIdTag("ID0413276BC1");
        Assert.assertEquals("IdTag TagID is 0413276BC1", "0413276BC1", r.getTagID());
    }

    public void testIdTagToString() {
        IdTag r = new DefaultIdTag("ID0413276BC1");
        Assert.assertEquals("IdTag toString is 0413276BC1", "0413276BC1", r.toString());

        r.setUserName("Test Tag");
        Assert.assertEquals("IdTag toString is 'Test Tag'", "Test Tag", r.toString());
    }

    public void testNotYetSeen() {
        IdTag r = new DefaultIdTag("ID0413276BC1");
        Assert.assertNull("At creation, Reporter where seen is null", r.getWhereLastSeen());
        Assert.assertNull("At creation, Date when seen is null", r.getWhenLastSeen());
        Assert.assertEquals("At creation, IdTag status is UNSEEN", IdTag.UNSEEN, r.getState());

        r.setWhereLastSeen(null);
        Assert.assertNull("After setWhereLastSeen(null), Reporter where seen is null", r.getWhereLastSeen());
        Assert.assertNull("After setWhereLastSeen(null), Date when seen is null", r.getWhenLastSeen());
        Assert.assertEquals("After setWhereLastSeen(null), IdTag status is UNSEEN", IdTag.UNSEEN, r.getState());
    }

    public void testHasBeenSeen() throws InterruptedException {
        IdTag r = new DefaultIdTag("ID0413276BC1");
        Reporter rep = new AbstractReporter("IR1") {
            public int getState() {
                return state;
            }

            public void setState(int s) {
                state = s;
            }
            int state = 0;
        };

        Date timeBefore = Calendar.getInstance().getTime();
        Thread.sleep(5);
        r.setWhereLastSeen(rep);
        Thread.sleep(5);
        Date timeAfter = Calendar.getInstance().getTime();

        Assert.assertEquals("Where last seen is 'IR1'", rep, r.getWhereLastSeen());
        Assert.assertNotNull("When last seen is not null", r.getWhenLastSeen());
        Assert.assertEquals("Status is SEEN", IdTag.SEEN, r.getState());
        Assert.assertTrue("Time when last seen is later than 'timeBefore'", r.getWhenLastSeen().after(timeBefore));
        Assert.assertTrue("Time when last seen is earlier than 'timeAfter'", r.getWhenLastSeen().before(timeAfter));

        r.setWhereLastSeen(null);
        Assert.assertNull("After setWhereLastSeen(null), Reporter where seen is null", r.getWhereLastSeen());
        Assert.assertNull("After setWhereLastSeen(null), Date when seen is null", r.getWhenLastSeen());
        Assert.assertEquals("After setWhereLastSeen(null), IdTag status is UNSEEN", IdTag.UNSEEN, r.getState());

    }

    // from here down is testing infrastructure
    public DefaultIdTagTest(String s) {
        super(s);
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initIdTagManager();
    }

    @Override
    protected void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DefaultIdTagTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DefaultIdTagTest.class);
        return suite;
    }

}
