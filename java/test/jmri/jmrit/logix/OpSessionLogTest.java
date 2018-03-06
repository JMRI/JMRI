package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assume;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.WindowOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class OpSessionLogTest {

    @Test
    public void openAndClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("logSession"));
            jdo.close();
        });
        t.setName("OpSessionLog File Chooser Dialog Close Thread");
        t.start();
        jmri.util.JmriJFrame f = new jmri.util.JmriJFrame("OpSessionLog Chooser Test");
        Assert.assertFalse(OpSessionLog.makeLogFile(f));
        f.dispose();
    }

    @Test
    @Ignore("needs more thought")
    public void makeLogFileCheck() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
       // This is going to be a graphical check.
       // make sure the log file is correctly chosen and created.
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

    // private final static Logger log = LoggerFactory.getLogger(OpSessionLogTest.class);

}
