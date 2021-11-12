package jmri.jmrit.display.palette;

import java.awt.Color;

import jmri.jmrit.display.*;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

/**
 *
 * @author Pete Cressman Copyright (C) 2018
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class ColorDialogTest {

    private ControlPanelEditor _cpe;
    private PositionableLabel _pos;

    @Test
    public void testCTor1() {
        _cpe.setBackgroundColor(Color.GREEN);
        assertThat(_cpe.getTargetPanel().getBackground()).withFailMessage("panel color is green").isEqualTo(Color.GREEN);

        DialogRunner dr = new DialogRunner(ColorDialog.ONLY, "PanelColor", Color.RED, "ButtonDone");
        dr.start();
        new ColorDialog(_cpe, _cpe.getTargetPanel(), ColorDialog.ONLY, null);
        Throwable thrown = catchThrowable(()-> dr.join());
        assertThat(thrown).isNull();

        new org.netbeans.jemmy.QueueTool().waitEmpty(50);  // allow some time for button push

        assertThat(_cpe.getTargetPanel().getBackground()).withFailMessage("panel color is red").isEqualTo(Color.RED);
    }

    @Test
    public void testCTor2() {
        _cpe.setBackgroundColor(Color.GREEN);
        assertThat(_cpe.getTargetPanel().getBackground()).withFailMessage("panel color is green").isEqualTo(Color.GREEN);

        DialogRunner dr = new DialogRunner(ColorDialog.ONLY, "PanelColor", Color.RED, "ButtonCancel");
        dr.start();
        new ColorDialog(_cpe, _cpe.getTargetPanel(), ColorDialog.ONLY, null);
        Throwable thrown = catchThrowable(()-> dr.join());
        assertThat(thrown).isNull();

        new org.netbeans.jemmy.QueueTool().waitEmpty(50);  // allow some time for button push

        assertThat(_cpe.getTargetPanel().getBackground()).withFailMessage("panel color is green").isEqualTo(Color.GREEN);
    }

    @Test
    public void testCTor3() {
        DialogRunner dr = new DialogRunner(ColorDialog.BORDER, "SetBorderSizeColor", Color.BLUE, "ButtonDone");
        dr.start();
        new ColorDialog(_cpe, _pos, ColorDialog.BORDER, null);
        Throwable thrown = catchThrowable(()-> dr.join());
        assertThat(thrown).isNull();

        new org.netbeans.jemmy.QueueTool().waitEmpty(50);  // allow some time for button push

        assertThat(_pos.getPopupUtility().getBorderColor()).withFailMessage("border color is blue").isEqualTo(Color.BLUE);
    }

    @Test
    public void testCTor4() {
        _pos.getPopupUtility().setBackgroundColor(Color.GREEN);
        assertThat(_pos.getPopupUtility().getBackground()).withFailMessage("margin color is green").isEqualTo(Color.GREEN);

        DialogRunner dr = new DialogRunner(ColorDialog.MARGIN, "SetMarginSizeColor", Color.RED, "ButtonCancel");
        dr.start();
        new ColorDialog(_cpe, _pos, ColorDialog.MARGIN, null);
        Throwable thrown = catchThrowable(()-> dr.join());
        assertThat(thrown).isNull();

        new org.netbeans.jemmy.QueueTool().waitEmpty(50);  // allow some time for button push

        assertThat(_pos.getPopupUtility().getBackground()).withFailMessage("margin color is green").isEqualTo(Color.GREEN);
    }

    @Test
    public void testCTor5() {
        DialogRunner dr = new DialogRunner(ColorDialog.FONT, "SetFontSizeColor", Color.RED, "ButtonDone");
        dr.start();
        new ColorDialog(_cpe, _pos, ColorDialog.FONT, null);
        Throwable thrown = catchThrowable(()-> dr.join());
        assertThat(thrown).isNull();

        new org.netbeans.jemmy.QueueTool().waitEmpty(50);  // allow some time for button push

        assertThat(_pos.getPopupUtility().getForeground()).withFailMessage("font color is red").isEqualTo(Color.RED);
    }

    @Test
    public void testCTor6() {
        DialogRunner dr = new DialogRunner(ColorDialog.TEXT, "SetTextSizeColor", Color.BLUE, "ButtonDone");
        dr.start();
        new ColorDialog(_cpe, _pos, ColorDialog.TEXT, null);
        Throwable thrown = catchThrowable(()-> dr.join());
        assertThat(thrown).isNull();

        new org.netbeans.jemmy.QueueTool().waitEmpty(50);  // allow some time for button push

        assertThat(_pos.getPopupUtility().getForeground()).withFailMessage("font color is red").isEqualTo(Color.BLUE);
    }

    void startEditor() throws Positionable.DuplicateIdException {
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
            assertThat(c).withFailMessage(_dialogTitle + " set color").isEqualTo(_color);
            jbo.pushNoBlock();
        }

    }

    @BeforeEach
    public void setUp() throws Positionable.DuplicateIdException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.resetProfileManager();
        startEditor();
    }

    @AfterEach
    public void tearDown() {
        EditorFrameOperator efo = new EditorFrameOperator(_cpe.getTargetFrame());
        efo.closeFrameWithConfirmations();
        EditorFrameOperator.clearEditorFrameOperatorThreads();

        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
