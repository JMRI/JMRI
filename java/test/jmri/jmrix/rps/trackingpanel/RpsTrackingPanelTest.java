package jmri.jmrix.rps.trackingpanel;

import java.awt.GraphicsEnvironment;
import javax.swing.BoxLayout;
import javax.vecmath.Point3d;
import jmri.InstanceManager;
import jmri.jmrit.roster.RosterConfigManager;
import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.Measurement;
import jmri.jmrix.rps.Model;
import jmri.jmrix.rps.Reading;
import jmri.jmrix.rps.Receiver;
import jmri.jmrix.rps.Region;
import jmri.jmrix.rps.RpsSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the rps.RpsTrackingPanel class.
 *
 * @author	Bob Jacobsen Copyright 2006
 */
public class RpsTrackingPanelTest {

    RpsSystemConnectionMemo memo = null;

    @Test
    public void testShow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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
        RpsTrackingPanel p = new RpsTrackingPanel(memo);
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

        Assert.assertNotNull("found frame", f);
        f.dispose();
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        memo = new RpsSystemConnectionMemo();
        InstanceManager.setDefault(RosterConfigManager.class, new RosterConfigManager());
    }

    @After
    public void tearDown() throws Exception {        JUnitUtil.tearDown();    }

}
