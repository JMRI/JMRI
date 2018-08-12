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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CsvExportActionTest.class);

}
