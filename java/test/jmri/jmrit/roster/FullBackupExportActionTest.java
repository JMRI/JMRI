package jmri.jmrit.roster;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class FullBackupExportActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriJFrame jf = new JmriJFrame("TestFullBackupWindow");
        jmri.util.swing.WindowInterface wi = jf;
        FullBackupExportAction t = new FullBackupExportAction("test full backup export",wi);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(jf);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(FullBackupExportActionTest.class);

}
