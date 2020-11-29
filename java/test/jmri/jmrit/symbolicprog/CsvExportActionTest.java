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
public class CsvExportActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CvTableModel tm = new CvTableModel(new JLabel(), null);
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("test Csv Export");
        CsvExportAction t = new CsvExportAction("Test Action",tm,jf);
        Assert.assertNotNull("exists",t);
        jf.dispose();
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CsvExportActionTest.class);

}
