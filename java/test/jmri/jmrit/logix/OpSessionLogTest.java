package jmri.jmrit.logix;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class OpSessionLogTest {

    private jmri.util.JmriJFrame f;
    private boolean retval;

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void openAndClose() {

        // create the window and make the log file on Swing thread
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            f = new jmri.util.JmriJFrame("OpSessionLog Chooser Test");

            // create a thread that waits to close the dialog box opened later
            Thread t = new Thread(() -> {
                // constructor for jdo will wait until the dialog is visible
                JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("logSession"));
                jdo.requestClose();
                jdo.waitClosed();
            });
            t.setName("OpSessionLog File Chooser Dialog Close Thread");
            t.start();

            // get the result of closing
            retval = OpSessionLog.makeLogFile(f);
        });

        // check results
        Assertions.assertFalse(retval);

        // done
        f.dispose();
    }

    @Test
    @Disabled("needs more thought")
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void makeLogFileCheck() {
       // This is going to be a graphical check.
       // make sure the log file is correctly chosen and created.
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        f = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(OpSessionLogTest.class);

}
