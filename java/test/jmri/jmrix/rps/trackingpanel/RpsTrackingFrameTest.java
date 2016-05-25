package jmri.jmrix.rps.trackingpanel;

import apps.tests.Log4JFixture;
import javax.swing.JFrame;
import javax.vecmath.Point3d;
import jmri.InstanceManager;
import jmri.jmrit.roster.RosterConfigManager;
import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.Measurement;
import jmri.jmrix.rps.Reading;
import jmri.jmrix.rps.Receiver;
import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the rps.RpsTrackingFrame class.
 *
 * @author	Bob Jacobsen Copyright 2008
 * @version	$Revision$
 */
public class RpsTrackingFrameTest extends TestCase {

    public void testShow() {
        new Engine() {
            void reset() {
                _instance = null;
            }
        }.reset();
        Engine.instance().setMaxReceiverNumber(2);
        Engine.instance().setReceiver(1, new Receiver(new Point3d(12., 12., 0.)));
        Engine.instance().setReceiver(2, new Receiver(new Point3d(12., 12., 0.)));

        RpsTrackingFrame f = new RpsTrackingFrame("Test RPS Tracking");
        f.initComponents();
        f.setVisible(true);

        RpsTrackingPanel p = f.panel; // use local access

        Reading loco = new Reading("21", null);
        Measurement m = new Measurement(loco, 0.0, 0.0, 0.0, 0.133, 5, "source");
        p.notify(m);

        loco = new Reading("21", null);
        m = new Measurement(loco, 5., 5., 0.0, 0.133, 5, "source");
        p.notify(m);

        loco = new Reading("21", null);
        m = new Measurement(loco, 0., 5., 0.0, 0.133, 5, "source");
        p.notify(m);

        loco = new Reading("21", null);
        m = new Measurement(loco, 5., 0., 0.0, 0.133, 5, "source");
        p.notify(m);

//    }
//  test order isn't guaranteed!
//    public void testXFrameCreation() {
        JFrame f2 = jmri.util.JmriJFrame.getFrame("Test RPS Tracking");
        Assert.assertTrue("found frame", f2 != null);
        f2.dispose();
    }

    // from here down is testing infrastructure
    public RpsTrackingFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {RpsTrackingFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RpsTrackingFrameTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        Log4JFixture.setUp();
        super.setUp();
        JUnitUtil.resetInstanceManager();
        InstanceManager.setDefault(RosterConfigManager.class, new RosterConfigManager());
    }

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        Log4JFixture.tearDown();
    }

}
