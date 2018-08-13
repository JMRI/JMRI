package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author Pete Cressman Copyright (C) 2018 
 */
public class ColorDialogTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditor cpe = new ControlPanelEditor("Fred");
        BlockedThread th = new BlockedThread(cpe);
        th.start();
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("ColorChooser"));
        JButtonOperator jbo = new JButtonOperator(jdo , jmri.jmrit.display.palette.Bundle.getMessage("ButtonDone"));
        jbo.push();     // why does it not push - ??
        JUnitUtil.dispose(cpe);
    }

    class BlockedThread extends Thread implements Runnable {
        ColorDialog _cd;
        ControlPanelEditor _cpe;
        
        BlockedThread(ControlPanelEditor ed) {
            _cpe = ed;
        }

        @Override
        public void run() {
            _cd = new ColorDialog(_cpe, _cpe.getTargetPanel(), null);
        }
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
