package jmri.jmrit.symbolicprog;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import javax.swing.JLabel;
import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Pr1ImportActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CvTableModel tm = new CvTableModel(new JLabel(), null);
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("test Pr1 Import");
        Pr1ImportAction t = new Pr1ImportAction("Test Action",tm,jf,new JLabel());
        Assert.assertNotNull("exists",t);
        jf.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.setUp();
    }

    // private final static Logger log = LoggerFactory.getLogger(Pr1ImportActionTest.class);

}
