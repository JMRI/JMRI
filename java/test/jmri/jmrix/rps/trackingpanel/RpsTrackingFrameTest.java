package jmri.jmrix.rps.trackingpanel;

import java.awt.GraphicsEnvironment;
import javax.vecmath.Point3d;
import jmri.InstanceManager;
import jmri.jmrit.roster.RosterConfigManager;
import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.Measurement;
import jmri.jmrix.rps.Reading;
import jmri.jmrix.rps.Receiver;
import jmri.jmrix.rps.RpsSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the rps.RpsTrackingFrame class.
 *
 * @author	Bob Jacobsen Copyright 2008
 */
public class RpsTrackingFrameTest {

    private RpsSystemConnectionMemo memo = null;

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
        Engine.instance().setReceiver(2, new Receiver(new Point3d(12., 12., 0.)));

        RpsTrackingFrame f = new RpsTrackingFrame("Test RPS Tracking",memo);
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

        Assert.assertNotNull("found frame", f);
        f.dispose();
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();

        memo = new RpsSystemConnectionMemo();
        InstanceManager.setDefault(RosterConfigManager.class, new RosterConfigManager());
    }

    @After
    public void tearDown() throws Exception {        JUnitUtil.tearDown();    }

}
