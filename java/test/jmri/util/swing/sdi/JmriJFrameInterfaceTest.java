package jmri.util.swing.sdi;

import javax.swing.JButton;
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.ButtonTestAction;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class JmriJFrameInterfaceTest {

    @Test
    public void testCTor() {
        JmriJFrameInterface t = new JmriJFrameInterface();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testAction() {
        JmriJFrame f = new JmriJFrame("SDI test");
        JButton b = new JButton(new ButtonTestAction(
                "new frame", new jmri.util.swing.sdi.JmriJFrameInterface()));
        f.add(b);
        f.pack();
        f.setVisible(true);
        JFrame f2 = jmri.util.JmriJFrame.getFrame("SDI test");
        Assert.assertTrue("found frame", f2 != null);
        JUnitUtil.dispose(f2);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JmriJFrameInterfaceTest.class);

}

