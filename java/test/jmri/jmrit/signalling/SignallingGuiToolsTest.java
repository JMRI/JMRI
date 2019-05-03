package jmri.jmrit.signalling;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SignallingGuiToolsTest {

    @Rule
    public jmri.util.junit.rules.RetryRule retryRule = new jmri.util.junit.rules.RetryRule(3);  // allow 3 retries of tests

    @Rule // This test class was periodically stalling and causing the CI run to time out. Limit its duration.
    public org.junit.rules.Timeout globalTimeout = org.junit.rules.Timeout.seconds(10);
    
    // the class under test is a collection of static methods for dealing with
    // signals in GUIs.

    @Test
    public void testShowAndCloseUpdateSignalmastLogicDialog() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SignallingFrame sf = new SignallingFrame();
        jmri.InstanceManager.getDefault(jmri.SignalMastManager.class);
        jmri.SignalMast sm1 = new jmri.implementation.VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
        jmri.SignalMast sm2 = new jmri.implementation.VirtualSignalMast("IF$vsm:basic:one-searchlight($2)");
        Thread t = new Thread(()-> {
           // constructor for jdo will wait until the dialog is visible
           JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("UpdateLogicTitle"));
           jdo.close();
        });
        t.setName("Close UpdateSignalMastLogic Thread");
        t.start();
        SignallingGuiTools.updateSignalMastLogic(sf,sm1,sm2);
        sf.dispose();
    }

    @Test
    public void testShowAndCloseSwapSignalmastLogicDialog() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SignallingFrame sf = new SignallingFrame();
        jmri.InstanceManager.getDefault(jmri.SignalMastManager.class);
        jmri.SignalMast sm1 = new jmri.implementation.VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
        jmri.SignalMast sm2 = new jmri.implementation.VirtualSignalMast("IF$vsm:basic:one-searchlight($2)");
        Thread t = new Thread(()-> {
           // constructor for jdo will wait until the dialog is visible
           JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("UpdateLogicTitle"));
           jdo.close();
        });
        t.setName("Close SwapSignalMastLogic Thread");
        t.start();
        SignallingGuiTools.swapSignalMastLogic(sf,sm1,sm2);
        sf.dispose();
    }

    @Test
    public void testShowAndCloseRemoveSignalmastLogicDialog() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SignallingFrame sf = new SignallingFrame();
        jmri.InstanceManager.getDefault(jmri.SignalMastManager.class);
        jmri.SignalMast sm1 = new jmri.implementation.VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
        Thread t = new Thread(()-> {
           // constructor for jdo will wait until the dialog is visible
           JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("RemoveLogicTitle"));
           jdo.close();
        });
        t.setName("Close RemoveSignalMastLogic Thread");
        t.start();
        SignallingGuiTools.removeSignalMastLogic(sf,sm1);
        sf.dispose();
    }

    @Test
    public void testShowAndCloseRemoveAlreadyAssignedSignalmastLogicDialog() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SignallingFrame sf = new SignallingFrame();
        jmri.InstanceManager.getDefault(jmri.SignalMastManager.class);
        jmri.SignalMast sm1 = new jmri.implementation.VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
        Thread t = new Thread(()-> {
           // constructor for jdo will wait until the dialog is visible
           JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("RemoveLogicTitle"));
           jdo.close();
        });
        t.setName("Close RemoveAlreadyAssignedSignalMastLogic Thread");
        t.start();
        SignallingGuiTools.removeAlreadyAssignedSignalMastLogic(sf,sm1);
        sf.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultSignalMastManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SignallingGuiToolsTest.class);

}
