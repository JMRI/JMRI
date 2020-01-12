package jmri.implementation;

import java.util.Calendar;
import java.util.Date;
import jmri.RailCom;
import jmri.Reporter;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the DefaultRailCom class
 *
 * @author Matthew Harris Copyright (C) 2011
 */
public class DefaultRailComTest {

    @Test
    public void testCreateRailCom() {
        RailCom r = new DefaultRailCom("ID1234");
        Assert.assertNotNull("RailCom not null", r);
    }

    @Test
    public void testGetRailComUserName() {
        RailCom r = new DefaultRailCom("ID1234", "Test Tag");
        Assert.assertEquals("RailCom user name is 'Test Tag'", "Test Tag", r.getUserName());
    }

    @Test
    public void testGetRailComTagID() {
        RailCom r = new DefaultRailCom("ID1234");
        Assert.assertEquals("RailCom TagID is 1234", "1234", r.getTagID());
    }

    @Test
    public void testRailComGetLocoAddress() {
        RailCom r = new DefaultRailCom("ID1234");
        Assert.assertEquals("Loco Address ", new jmri.DccLocoAddress(1234,true), r.getLocoAddress());
    }

    @Test
    public void testRailComGetDccLocoAddress() {
        // this is testing a now deprecated default method in
        // the RailCom interface.  For code coverage, we need to
        // leave this until the deprecated method can be removed.
        RailCom r = new DefaultRailCom("ID1234");
        Assert.assertEquals("Dcc Loco Address ", new jmri.DccLocoAddress(1234,true), r.getDccLocoAddress());
    }

    @Test
    public void testRailComToString() {
        RailCom r = new DefaultRailCom("ID1234");
        Assert.assertEquals("RailCom toString ", "ID1234", r.toString());
    }

    @Test
    public void testRailComToReportString() {
        DefaultRailCom r = new DefaultRailCom("ID1234");
        Assert.assertEquals("RailCom toReportString ", "Unknown Orientation Address 1234(L) ", r.toReportString());
    }

    @Test
    public void testNotYetSeen() {
        RailCom r = new DefaultRailCom("ID0413276BC1");
        Assert.assertNull("At creation, Reporter where seen is null", r.getWhereLastSeen());
        Assert.assertNull("At creation, Date when seen is null", r.getWhenLastSeen());
        Assert.assertEquals("At creation, RailCom status is UNSEEN", RailCom.UNSEEN, r.getState());

        r.setWhereLastSeen(null);
        Assert.assertNull("After setWhereLastSeen(null), Reporter where seen is null", r.getWhereLastSeen());
        Assert.assertNull("After setWhereLastSeen(null), Date when seen is null", r.getWhenLastSeen());
        Assert.assertEquals("After setWhereLastSeen(null), RailCom status is UNSEEN", RailCom.UNSEEN, r.getState());
    }

    @Test
    public void testHasBeenSeen() throws InterruptedException {
        RailCom r = new DefaultRailCom("ID0413276BC1");
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
        Assert.assertEquals("Status is SEEN", RailCom.SEEN, r.getState());
        Assert.assertTrue("Time when last seen is later than 'timeBefore'", r.getWhenLastSeen().after(timeBefore));
        Assert.assertTrue("Time when last seen is earlier than 'timeAfter'", r.getWhenLastSeen().before(timeAfter));

        r.setWhereLastSeen(null);
        Assert.assertNull("After setWhereLastSeen(null), Reporter where seen is null", r.getWhereLastSeen());
        Assert.assertNull("After setWhereLastSeen(null), Date when seen is null", r.getWhenLastSeen());
        Assert.assertEquals("After setWhereLastSeen(null), RailCom status is UNSEEN", RailCom.UNSEEN, r.getState());

    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initRailComManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.clearShutDownManager(); // would be better to check and clean up specifics in tests
        JUnitUtil.tearDown();
    }

}
