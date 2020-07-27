package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.GraphicsEnvironment;

import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author Pete Cressman Copyright (C) 2018 
 */
public class ColorDialogTest {

    ControlPanelEditor _cpe;
    PositionableLabel _pos;
    boolean _done;
    
    @Test
    public void testCTor1() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        startEditor();
        _cpe.setBackgroundColor(Color.GREEN);
        Assert.assertEquals("panel color is green", Color.GREEN, _cpe.getTargetPanel().getBackground());

        _done = false;
        DialogRunner dr = new DialogRunner(ColorDialog.ONLY, "PanelColor", Color.RED, "ButtonDone");
        dr.start();
        Thread t = new Thread(() -> {
            new ColorDialog(_cpe, _cpe.getTargetPanel(), ColorDialog.ONLY, null);
        });
        t.start();
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return _done;
        }, "Dialog done.");
        new org.netbeans.jemmy.QueueTool().waitEmpty(50);  // allow some time for button push

        Assert.assertEquals("panel color is red", Color.RED, _cpe.getTargetPanel().getBackground());
        JUnitUtil.dispose(_cpe);
    }

    @Test
    public void testCTor2() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        startEditor();
        _cpe.setBackgroundColor(Color.GREEN);
        Assert.assertEquals("panel color is green", Color.GREEN, _cpe.getTargetPanel().getBackground());

        _done = false;
        DialogRunner dr = new DialogRunner(ColorDialog.ONLY, "PanelColor", Color.RED, "ButtonCancel");
        dr.start();
        Thread t = new Thread(() -> {
            new ColorDialog(_cpe, _cpe.getTargetPanel(), ColorDialog.ONLY, null);
        });
        t.start();
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return _done;
        }, "Dialog done.");
        new org.netbeans.jemmy.QueueTool().waitEmpty(50);  // allow some time for button push

        Assert.assertEquals("panel color is green", Color.GREEN, _cpe.getTargetPanel().getBackground());
        JUnitUtil.dispose(_cpe);
    }

    @Test
    public void testCTor3() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        startEditor();
        _done = false;
        DialogRunner dr = new DialogRunner(ColorDialog.BORDER, "SetBorderSizeColor", Color.BLUE, "ButtonDone");
        dr.start();
        Thread t = new Thread(() -> {
            new ColorDialog(_cpe, _pos, ColorDialog.BORDER, null);
        });
        t.start();
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return _done;
        }, "Dialog done.");
        new org.netbeans.jemmy.QueueTool().waitEmpty(50);  // allow some time for button push

        Assert.assertEquals("border color is blue", Color.BLUE, _pos.getPopupUtility().getBorderColor());
        JUnitUtil.dispose(_cpe);
    }

    @Test
    public void testCTor4() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        startEditor();
        _pos.getPopupUtility().setBackgroundColor(Color.GREEN);
        Assert.assertEquals("margin color is green", Color.GREEN, _pos.getPopupUtility().getBackground());

        _done = false;
        DialogRunner dr = new DialogRunner(ColorDialog.MARGIN, "SetMarginSizeColor", Color.RED, "ButtonCancel");
        dr.start();
        Thread t = new Thread(() -> {
            new ColorDialog(_cpe, _pos, ColorDialog.MARGIN, null);
        });
        t.start();
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return _done;
        }, "Dialog done.");
        new org.netbeans.jemmy.QueueTool().waitEmpty(50);  // allow some time for button push

        Assert.assertEquals("margin color is green", Color.GREEN, _pos.getPopupUtility().getBackground());
        JUnitUtil.dispose(_cpe);
    }

    @Test
    public void testCTor5() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        startEditor();

        _done = false;
        DialogRunner dr = new DialogRunner(ColorDialog.FONT, "SetFontSizeColor", Color.RED, "ButtonDone");
        dr.start();
        Thread t = new Thread(() -> {
            new ColorDialog(_cpe, _pos, ColorDialog.FONT, null);
        });
        t.start();
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return _done;
        }, "Dialog done.");
        new org.netbeans.jemmy.QueueTool().waitEmpty(50);  // allow some time for button push

        Assert.assertEquals("font color is red", Color.RED, _pos.getPopupUtility().getForeground());
        JUnitUtil.dispose(_cpe);
    }

    @Test
    public void testCTor6() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        startEditor();

        _done = false;
        DialogRunner dr = new DialogRunner(ColorDialog.TEXT, "SetTextSizeColor", Color.BLUE, "ButtonDone");
        dr.start();
        Thread t = new Thread(() -> {
            new ColorDialog(_cpe, _pos, ColorDialog.TEXT, null);
        });
        t.start();
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return _done;
        }, "Dialog done.");
        new org.netbeans.jemmy.QueueTool().waitEmpty(50);  // allow some time for button push

        Assert.assertEquals("font color is red", Color.BLUE, _pos.getPopupUtility().getForeground());
        JUnitUtil.dispose(_cpe);
    }

    void startEditor() {
        _cpe = new ControlPanelEditor("Fred");
        _pos = new PositionableLabel("Some Text", _cpe);
        _cpe.putItem(_pos);
    }

    class DialogRunner extends Thread {
        // constructor for jdo will wait until the dialog is visible
        String _dialogTitle;
        String _buttonTitle;
        int _type;
        Color _color;

        DialogRunner(int type, String dialogTitle, Color color, String buttonTitle) {
            super();
            _type = type;
            _color = color;
            _dialogTitle = dialogTitle;
            _buttonTitle = buttonTitle;
        }

        @Override
        public void run() {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(Bundle.getMessage(_dialogTitle));
            JButtonOperator jbo = new JButtonOperator(jdo, Bundle.getMessage(_buttonTitle));
            ColorDialog cd = (ColorDialog)jdo.getSource();
            cd._chooser.setColor(_color);

            Color c;
            switch (_type) {
                case ColorDialog.ONLY: 
                    c = _cpe.getTargetPanel().getBackground();
                    break;
                case ColorDialog.BORDER:
                    c = _pos.getPopupUtility().getBorderColor();
                    break;
                case ColorDialog.MARGIN:
                    c = _pos.getPopupUtility().getBackground();
                    break;
                case ColorDialog.TEXT:
                case ColorDialog.FONT:
                    c = _pos.getPopupUtility().getForeground();
                    break;
                default:
                    c = Color.BLACK;
            }
            Assert.assertEquals(_dialogTitle + " set color", _color, c);
            jbo.pushNoBlock();
            _done = true;
        }

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
