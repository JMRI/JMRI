package jmri.jmrit.signalling;

import jmri.util.ThreadingUtil;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
@Timeout(10) // This test class was periodically stalling and causing the CI run to time out. Limit its duration.
public class SignallingGuiToolsTest {

    // the class under test is a collection of static methods for dealing with
    // signals in GUIs.

    @Test
    public void testShowAndCloseUpdateSignalmastLogicDialog() {
        SignallingFrame sf = new SignallingFrame();
        jmri.InstanceManager.getDefault(jmri.SignalMastManager.class);
        jmri.SignalMast sm1 = new jmri.implementation.VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
        jmri.SignalMast sm2 = new jmri.implementation.VirtualSignalMast("IF$vsm:basic:one-searchlight($2)");
        Thread t = new Thread(()-> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("UpdateLogicTitle"));
            JButtonOperator jbo = new JButtonOperator(jdo,"Update");
            ThreadingUtil.runOnGUI(() -> jbo.push() );
            jdo.waitClosed();
        });
        t.setName("Close UpdateSignalMastLogic Thread");
        t.start();
        SignallingGuiTools.updateSignalMastLogic(sf,sm1,sm2);
        JUnitUtil.waitFor(() -> !t.isAlive(), "Dialogue thread complete");
        sf.dispose();
    }

    @Test
    public void testShowAndCloseSwapSignalmastLogicDialog() {
        SignallingFrame sf = new SignallingFrame();
        jmri.InstanceManager.getDefault(jmri.SignalMastManager.class);
        jmri.SignalMast sm1 = new jmri.implementation.VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
        jmri.SignalMast sm2 = new jmri.implementation.VirtualSignalMast("IF$vsm:basic:one-searchlight($2)");
        Thread t = new Thread(()-> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("UpdateLogicTitle"));
            JButtonOperator jbo = new JButtonOperator(jdo,"Update");
            ThreadingUtil.runOnGUI(() -> jbo.push() );
            jdo.waitClosed();
        });
        t.setName("Close SwapSignalMastLogic Thread");
        t.start();
        SignallingGuiTools.swapSignalMastLogic(sf,sm1,sm2);
        JUnitUtil.waitFor(() -> !t.isAlive(), "Dialogue thread complete");
        sf.dispose();
    }

    @Test
    public void testShowAndCloseRemoveSignalmastLogicDialog() {
        SignallingFrame sf = new SignallingFrame();
        jmri.InstanceManager.getDefault(jmri.SignalMastManager.class);
        jmri.SignalMast sm1 = new jmri.implementation.VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
        Thread t = new Thread(()-> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("RemoveLogicTitle"));
            JButtonOperator jbo = new JButtonOperator(jdo,"Remove");
            ThreadingUtil.runOnGUI(() -> jbo.push() );
            jdo.waitClosed();
        });
        t.setName("Close RemoveSignalMastLogic Thread");
        t.start();
        SignallingGuiTools.removeSignalMastLogic(sf,sm1);
        JUnitUtil.waitFor(() -> !t.isAlive(), "Dialogue thread complete");
        sf.dispose();
    }

    @Test
    public void testShowAndCloseRemoveAlreadyAssignedSignalmastLogicDialog() {
        SignallingFrame sf = new SignallingFrame();
        jmri.InstanceManager.getDefault(jmri.SignalMastManager.class);
        jmri.SignalMast sm1 = new jmri.implementation.VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
        Thread t = new Thread(()-> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("RemoveLogicTitle"));
            JButtonOperator jbo = new JButtonOperator(jdo,"Remove");
            ThreadingUtil.runOnGUI(() -> jbo.push() );
            jdo.waitClosed();
        });
        t.setName("Close RemoveAlreadyAssignedSignalMastLogic Thread");
        t.start();
        SignallingGuiTools.removeAlreadyAssignedSignalMastLogic(sf,sm1);
        JUnitUtil.waitFor(() -> !t.isAlive(), "Dialogue thread complete");
        sf.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultSignalMastManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SignallingGuiToolsTest.class);

}
