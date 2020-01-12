package jmri.jmrix.rps;

import javax.vecmath.Point3d;
import jmri.Sensor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the RPS Sensor class.
 *
 * @author	Bob Jacobsen Copyright 2007
 */
public class RpsSensorTest extends jmri.implementation.AbstractSensorTestBase {

    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkOnMsgSent() {}

    @Override
    public void checkOffMsgSent() {}

    @Override
    public void checkStatusRequestMsgSent() {}

    @Test
    public void testPoints() {
        Region r1 = new Region(new Point3d[]{
            new Point3d(0., 0., 0.),
            new Point3d(1., 0., 0.),
            new Point3d(1., 1., 0.),
            new Point3d(0., 1., 0.)}
        );

        RpsSensor s = new RpsSensor("RS(0,0,0);(1,0,0);(1,1,0);(0,1,0)", "R");

        Assert.assertTrue("sensor matches region", r1.equals(s.getRegion()));
    }

    @Test
    public void testOperation() {
        RpsSensor s = new RpsSensor("RS(0,0,0);(1,0,0);(1,1,0);(0,1,0)", "R");

        Assert.assertTrue("1: not active", s.getKnownState() == Sensor.UNKNOWN);

        Reading loco = new Reading("21", null);
        Measurement m = new Measurement(loco, 0.5, 0.5, 0.0, 0.133, 3, "source");
        s.notify(m);
        Assert.assertTrue("2: active", s.getKnownState() == Sensor.ACTIVE);

        m = new Measurement(loco, -0.5, 0.5, 0.0, 0.133, 3, "source");
        s.notify(m);
        Assert.assertTrue("3: inactive", s.getKnownState() == Sensor.INACTIVE);
    }

    @Test
    public void testTwoLocos() {
        RpsSensor s = new RpsSensor("RS(0,0,0);(1,0,0);(1,1,0);(0,1,0)", "R");

        Assert.assertTrue("1: not active", s.getKnownState() == Sensor.UNKNOWN);

        Reading loco1 = new Reading("21", null);
        Reading loco2 = new Reading("34", null);

        Measurement m = new Measurement(loco1, 0.5, 0.5, 0.0, 0.133, 3, "source");
        s.notify(m);
        Assert.assertTrue("2: active", s.getKnownState() == Sensor.ACTIVE);

        m = new Measurement(loco2, 0.5, 0.5, 0.0, 0.133, 3, "source");
        s.notify(m);
        Assert.assertTrue("3: active", s.getKnownState() == Sensor.ACTIVE);

        // one loco leaves, but other is still present
        m = new Measurement(loco1, -0.5, 0.5, 0.0, 0.133, 3, "source");
        s.notify(m);
        Assert.assertTrue("4: active", s.getKnownState() == Sensor.ACTIVE);

        // second leaves
        m = new Measurement(loco2, -0.5, 0.5, 0.0, 0.133, 3, "source");
        s.notify(m);
        Assert.assertTrue("5: inactive", s.getKnownState() == Sensor.INACTIVE);
    }

    @Test
    public void testModel() {
        // clear Model to create a new one
        new Model() {
            void reset() {
                _instance = null;
            }
        }.reset();
        // create sensor
        RpsSensor s = new RpsSensor("RS(0,0,0);(1,0,0);(1,1,0);(0,1,0)", "R");
        // check for Region
        Assert.assertNotNull("exists", s);
        Assert.assertTrue("1 region", Model.instance().getRegions().size() == 1);
        Assert.assertTrue("equal",
                new Region("(0,0,0);(1,0,0);(1,1,0);(0,1,0)").equals(
                        Model.instance().getRegions().get(0))
        );
    }

    @Override
    @Before
    public void setUp(){
        JUnitUtil.setUp();	
        t = new RpsSensor("RS(0,0,0);(1,0,0);(1,1,0);(0,1,0)", "R");
    }

    @Override
    @After
    public void tearDown(){
        t.dispose();
        JUnitUtil.tearDown();
    }

}

