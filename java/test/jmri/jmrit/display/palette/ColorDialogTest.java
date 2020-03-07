package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;
import javax.swing.JComponent;
import jmri.jmrit.display.PositionableLabel;
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

    ControlPanelEditor _cpe;
    PositionableLabel _pos;
    
    @Test
    public void testCTor1() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        startEditor();
        BlockedThread th = new BlockedThread(_cpe, _cpe.getTargetPanel(), ColorDialog.ONLY);
        th.start();
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("PanelColor"));
        JButtonOperator jbo = new JButtonOperator(jdo , jmri.jmrit.display.palette.Bundle.getMessage("ButtonDone"));
        jbo.push();     // why does it not push - ??
        JUnitUtil.dispose(_cpe);
    }

    @Test
    public void testCTor2() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        startEditor();
        BlockedThread th = new BlockedThread(_cpe, _pos, ColorDialog.BORDER);
        th.start();
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("SetBorderSizeColor"));
        JButtonOperator jbo = new JButtonOperator(jdo , jmri.jmrit.display.palette.Bundle.getMessage("ButtonDone"));
        jbo.push();     // why does it not push - ??
        JUnitUtil.dispose(_cpe);
    }

    @Test
    public void testCTor3() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        startEditor();
        BlockedThread th = new BlockedThread(_cpe, _pos, ColorDialog.MARGIN);
        th.start();
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("SetMarginSizeColor"));
        JButtonOperator jbo = new JButtonOperator(jdo , jmri.jmrit.display.palette.Bundle.getMessage("ButtonDone"));
        jbo.push();     // why does it not push - ??
        JUnitUtil.dispose(_cpe);
    }

    @Test
    public void testCTor4() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        startEditor();
        BlockedThread th = new BlockedThread(_cpe, _pos, ColorDialog.FONT);
        th.start();
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("SetFontSizeColor"));
        JButtonOperator jbo = new JButtonOperator(jdo , jmri.jmrit.display.palette.Bundle.getMessage("ButtonDone"));
        jbo.push();     // why does it not push - ??
        JUnitUtil.dispose(_cpe);
    }

    @Test
    public void testCTor5() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        startEditor();
        BlockedThread th = new BlockedThread(_cpe, _pos, ColorDialog.TEXT);
        th.start();
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("SetTextSizeColor"));
        JButtonOperator jbo = new JButtonOperator(jdo , jmri.jmrit.display.palette.Bundle.getMessage("ButtonDone"));
        jbo.push();     // why does it not push - ??
        JUnitUtil.dispose(_cpe);
    }

    void startEditor() {
        _cpe = new ControlPanelEditor("Fred");
        _pos = new PositionableLabel("Some Text", _cpe);
        _cpe.putItem(_pos);
    }

    class BlockedThread extends Thread {
        ColorDialog _cd;
        ControlPanelEditor _cpe;
        JComponent _target;
        int _type;
        
        BlockedThread(ControlPanelEditor ed, JComponent target, int type) {
            _cpe = ed;
            _target = target;
            _type = type;
        }

        @Override
        public void run() {
            _cd = new ColorDialog(_cpe, _target, _type, null);
        }
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
