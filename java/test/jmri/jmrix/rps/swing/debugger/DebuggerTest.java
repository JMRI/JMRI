package jmri.jmrix.rps.swing.debugger;

import javax.vecmath.Point3d;
import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.Measurement;
import jmri.jmrix.rps.Reading;
import jmri.jmrix.rps.Receiver;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.rps.swing.debugger package
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class DebuggerTest extends TestCase {

    public void testCtor() throws Exception {

        // create a context
        Engine.instance().setMaxReceiverNumber(2);
        Engine.instance().setReceiver(1, new Receiver(new Point3d(1, 2, 3)));
        Engine.instance().setReceiver(2, new Receiver(new Point3d(1, 2, 3)));

        Reading r = new Reading("21", new double[]{11, 12, 13, 14});
        Measurement m = new Measurement(r, -0.5, 0.5, 0.0, 0.133, 3, "source");

        // show frame
        DebuggerFrame f = new DebuggerFrame();
        f.initComponents();
        f.setVisible(true);

        // data
        f.notify(r);
        f.notify(m);

        // close
        f.dispose();
    }

    // from here down is testing infrastructure
    public DebuggerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DebuggerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(DebuggerTest.class);
        return suite;
    }

}
