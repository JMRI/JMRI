package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;
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

    jmri.util.JmriJFrame f;
    boolean retval;
    
    @Test
    public void openAndClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
                
        // create the window and make the log file on Swing thread
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            f = new jmri.util.JmriJFrame("OpSessionLog Chooser Test");
            
            // create a thread that waits to close the dialog box opened later
            Thread t = new Thread(() -> {
                // constructor for jdo will wait until the dialog is visible
                JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("logSession"));
                jdo.close();
            });
            t.setName("OpSessionLog File Chooser Dialog Close Thread");
            t.start();

            // get the result of closing
            retval = OpSessionLog.makeLogFile(f);
        });
        
        // check results
        Assert.assertFalse(retval);
        
        // done
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
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        f = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(OpSessionLogTest.class);

}
