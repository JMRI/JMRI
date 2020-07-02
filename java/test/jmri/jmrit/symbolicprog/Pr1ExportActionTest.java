package jmri.jmrit.symbolicprog;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import javax.swing.JLabel;

import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Pr1ExportActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CvTableModel tm = new CvTableModel(new JLabel(), null);
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("test Pr1 Export");
        Pr1ExportAction t = new Pr1ExportAction("Test Action",tm,jf);
        Assert.assertNotNull("exists",t);
        jf.dispose();
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(Pr1ExportActionTest.class);

}
