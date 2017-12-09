package jmri.jmrix.rps.swing.debugger;

import java.awt.GraphicsEnvironment;
import javax.vecmath.Point3d;
import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.Measurement;
import jmri.jmrix.rps.Reading;
import jmri.jmrix.rps.Receiver;
import jmri.jmrix.rps.RpsSystemConnectionMemo;
import org.junit.Assume;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.rps.swing.debugger package
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class DebuggerTest {

    private RpsSystemConnectionMemo memo = null;

    @Test
    public void testCtor() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // create a context
        Engine.instance().setMaxReceiverNumber(2);
        Engine.instance().setReceiver(1, new Receiver(new Point3d(1, 2, 3)));
        Engine.instance().setReceiver(2, new Receiver(new Point3d(1, 2, 3)));

        Reading r = new Reading("21", new double[]{11, 12, 13, 14});
        Measurement m = new Measurement(r, -0.5, 0.5, 0.0, 0.133, 3, "source");

        // show frame
        DebuggerFrame f = new DebuggerFrame(memo);
        f.initComponents();
        f.setVisible(true);

        // data
        f.notify(r);
        f.notify(m);

        Assert.assertNotNull("exists",f);

        // close
        f.dispose();
    }

    @Before
    public void setUp(){
        jmri.util.JUnitUtil.setUp();
        memo = new RpsSystemConnectionMemo();
    }

    @After
    public void tearDown(){
        jmri.util.JUnitUtil.tearDown();
    }

}
