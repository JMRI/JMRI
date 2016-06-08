package jmri.jmrix.rps.trackingpanel;

import apps.tests.Log4JFixture;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.vecmath.Point3d;
import jmri.InstanceManager;
import jmri.jmrit.roster.RosterConfigManager;
import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.Measurement;
import jmri.jmrix.rps.Model;
import jmri.jmrix.rps.Reading;
import jmri.jmrix.rps.Receiver;
import jmri.jmrix.rps.Region;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the rps.RpsTrackingPanel class.
 *
 * @author	Bob Jacobsen Copyright 2006
 * @version	$Revision$
 */
public class RpsTrackingPanelTest extends TestCase {

    public void testShow() {
        new Engine() {
            void reset() {
                _instance = null;
            }
        }.reset();
        Engine.instance().setMaxReceiverNumber(2);
        Engine.instance().setReceiver(1, new Receiver(new Point3d(12., 12., 0.)));
        Engine.instance().setReceiver(2, new Receiver(new Point3d(13., 13., 0.)));

        JmriJFrame f = new JmriJFrame("Test Tracking Panel");
        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));
        RpsTrackingPanel p = new RpsTrackingPanel();
        p.setSize(400, 400);
        p.setOrigin(0, 0);
        p.setCoordMax(30., 30.);
        f.getContentPane().add(p);
        f.pack();

        // add some regions to probe corners
        Region r = new Region("(4,4,0);(10,16,0);(18,10,0);(4,4,0)");
        Model.instance().addRegion(r);

        r = new Region("(30,15,0);(25,15,0);(25,20,0);(30,15,0)");
        Model.instance().addRegion(r);

        r = new Region("(15,30,0);(15,25,0);(20,25,0);(15,30,0)");
        Model.instance().addRegion(r);

        r = new Region("(25,25,0);(25,28,0);(30,30,1);(29,25,0);(25,25,0)");
        Model.instance().addRegion(r);

        // show overlap
        r = new Region("(20,20,0);(22,20,0);(22,22,1);(20,22,0)");
        Model.instance().addRegion(r);
        r = new Region("(19,19,0);(21,19,0);(21,21,1);(19,21,0)");
        Model.instance().addRegion(r);

        // show panel
        f.setSize(400, 400);
        f.setVisible(true);

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

        // check separate locos
        int NUM_LOCO = 64;
        for (int i = 0; i < NUM_LOCO; i++) {
            loco = new Reading("" + i, null);
            m = new Measurement(loco, 6. + 1. * i, 0., 0.0, 0.133, 5, "source");
            p.notify(m);
            m = new Measurement(loco, 6. + 1. * i, 12., 0.0, 0.133, 5, "source");
            p.notify(m);
        }

//    }
//  test order isn't guaranteed!
//    public void testXFrameCreation() {
        JFrame f2 = jmri.util.JmriJFrame.getFrame("Test Tracking Panel");
        Assert.assertTrue("found frame", f2 != null);
        f2.dispose();
    }

    // from here down is testing infrastructure
    public RpsTrackingPanelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {RpsTrackingPanelTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RpsTrackingPanelTest.class);
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
