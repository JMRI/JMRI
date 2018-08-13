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

    ControlPanelEditor cpe;
    
    @Test
    public void testCTor1() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        BlockedThread th = new BlockedThread(cpe, ColorDialog.ONLY);
        th.start();
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("ColorChooser"));
        JButtonOperator jbo = new JButtonOperator(jdo , jmri.jmrit.display.palette.Bundle.getMessage("ButtonDone"));
        jbo.push();     // why does it not push - ??
    }

    @Test
    public void testCTor2() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        BlockedThread th = new BlockedThread(cpe, ColorDialog.BORDER);
        th.start();
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("ColorChooser"));
        JButtonOperator jbo = new JButtonOperator(jdo , jmri.jmrit.display.palette.Bundle.getMessage("ButtonDone"));
        jbo.push();     // why does it not push - ??
    }

    @Test
    public void testCTor3() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        BlockedThread th = new BlockedThread(cpe, ColorDialog.MARGIN);
        th.start();
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("ColorChooser"));
        JButtonOperator jbo = new JButtonOperator(jdo , jmri.jmrit.display.palette.Bundle.getMessage("ButtonDone"));
        jbo.push();     // why does it not push - ??
    }

    @Test
    public void testCTor4() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        BlockedThread th = new BlockedThread(cpe, ColorDialog.FONT);
        th.start();
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("ColorChooser"));
        JButtonOperator jbo = new JButtonOperator(jdo , jmri.jmrit.display.palette.Bundle.getMessage("ButtonDone"));
        jbo.push();     // why does it not push - ??
    }

    @Test
    public void testCTor5() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        BlockedThread th = new BlockedThread(cpe, ColorDialog.TEXT);
        th.start();
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("ColorChooser"));
        JButtonOperator jbo = new JButtonOperator(jdo , jmri.jmrit.display.palette.Bundle.getMessage("ButtonDone"));
        jbo.push();     // why does it not push - ??
    }

    class BlockedThread extends Thread implements Runnable {
        ColorDialog _cd;
        ControlPanelEditor _cpe;
        int _type;
        
        BlockedThread(ControlPanelEditor ed, int type) {
            _cpe = ed;
            _type = type;
        }

        @Override
        public void run() {
            _cd = new ColorDialog(_cpe, _cpe.getTargetPanel(), _type, null);
        }
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        cpe = new ControlPanelEditor("Fred");
    }

    @After
    public void tearDown() {
        JUnitUtil.dispose(cpe);
        JUnitUtil.tearDown();
    }
}
