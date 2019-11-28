package jmri.implementation;

import java.util.Calendar;
import java.util.Date;
import jmri.IdTag;
import jmri.Reporter;
import jmri.Reportable;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the DefaultIdTag class
 *
 * @author Matthew Harris Copyright (C) 2011
 */
public class DefaultIdTagTest {

    @Test
    public void testCreateIdTag() {
        IdTag r = new DefaultIdTag("ID0413276BC1");
        Assert.assertNotNull("IdTag not null", r);
    }

    @Test
    public void testGetIdTagUserName() {
        IdTag r = new DefaultIdTag("ID0413276BC1", "Test Tag");
        Assert.assertEquals("IdTag user name is 'Test Tag'", "Test Tag", r.getUserName());
    }

    @Test
    public void testGetIdTagTagID() {
        IdTag r = new DefaultIdTag("ID0413276BC1");
        Assert.assertEquals("IdTag TagID is 0413276BC1", "0413276BC1", r.getTagID());
    }

    @Test
    public void testIdTagToString() {
        IdTag r = new DefaultIdTag("ID0413276BC1");
        Assert.assertEquals("IdTag toString is ID0413276BC1", "ID0413276BC1", r.toString());
    }

    @Test
    public void testIdTagToReportString() {
        DefaultIdTag r = new DefaultIdTag("ID0413276BC1");
        Assert.assertEquals("IdTag toReportString is 0413276BC1", "0413276BC1", r.toReportString());

        r.setUserName("Test Tag");
        Assert.assertEquals("IdTag toReportString is 'Test Tag'", "Test Tag", r.toReportString());
    }

    @Test
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

    @Test
    public void testHasBeenSeen() throws InterruptedException {
        IdTag r = new DefaultIdTag("ID0413276BC1");
        Reporter rep = new AbstractReporter("IR1") {
            @Override
            public int getState() {
                return state;
            }

            @Override
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

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initIdTagManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.clearShutDownManager(); // should be converted to check of scheduled ShutDownActions
        JUnitUtil.tearDown();
    }

}
