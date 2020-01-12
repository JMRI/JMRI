package jmri.jmrix.bachrus;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class DccSpeedProfileTest {

    private DccSpeedProfile profile = null;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",profile);
    }

    @Test
    public void testGetLength(){
        Assert.assertEquals("length",28,profile.getLength());
    }

    @Test
    public void testSetAndGetPoint(){
        Assert.assertTrue("point set",profile.setPoint(0,20.5f));
        Assert.assertEquals("point 0 after set",20.5,profile.getPoint(0),0.0);
        Assert.assertTrue("maximum after point 0 set",20.5<=profile.getMax());
    }

    @Test
    public void testSetAndGetPointOutOfOrder(){
        // the code enforces setting and getting the points in order.
        Assert.assertTrue("point set",profile.setPoint(5,20.5f));
        // the previous set works, but the get won't report the value entered.
        Assert.assertEquals("point 5 after set",-1.0,profile.getPoint(5),0.0);
    }

    @Test
    public void testSetAndGetMax(){
        profile.setMax(20.5f);
        // maximum values are in incrememnts of 20, but all we really need to
        // check is that it is at leat the value we set.
        Assert.assertTrue("maximum after set",20.5<=profile.getMax());
    }

    @Test
    public void testGetLastPoint(){
        Assert.assertEquals("last point",-1,profile.getLast());
    }

    @Test
    public void testClear(){
        Assert.assertTrue("point 0 set",profile.setPoint(0,50.0f));
        Assert.assertEquals("point 0 before clear",50.0,profile.getPoint(0),0.0);
        Assert.assertTrue("point 1 set",profile.setPoint(1,100.0f));
        Assert.assertEquals("point 1 before clear",100.0,profile.getPoint(1),0.0);
        Assert.assertTrue("before clear, max value",100.0<=profile.getMax());
        Assert.assertEquals("before clear, last value",1,profile.getLast());
        profile.clear();
        Assert.assertEquals("point 0 after clear",-1.0,profile.getPoint(0),0.0);
        Assert.assertEquals("point 1 after clear",-1.0,profile.getPoint(1),0.0);
        Assert.assertEquals("after clear, max value",40.0,profile.getMax(),0.0);
        Assert.assertEquals("after clear, last value",-1,profile.getLast());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        profile = new DccSpeedProfile(28);
    }

    @After
    public void tearDown() {
        profile = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DccSpeedProfileTest.class);

}
