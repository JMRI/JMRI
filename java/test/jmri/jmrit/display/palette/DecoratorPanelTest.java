package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.GraphicsEnvironment;

import jmri.jmrit.display.EditorScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.PositionablePopupUtil;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DecoratorPanelTest {

    EditorScaffold editor;
    DisplayFrame df;
    ControlPanelEditor _cpe;
    PositionableLabel _pos;
    boolean _done;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        editor = new EditorScaffold("Editor");
        df = new DisplayFrame("DisplayFrame", editor);
        DecoratorPanel dec = new DecoratorPanel(df);
        Assert.assertNotNull("exists", dec);
        df.dispose();
    }

    @Test
    public void testInit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        editor = new EditorScaffold("Editor");
        df = new DisplayFrame("DisplayFrame", editor);
        DecoratorPanel dec = new DecoratorPanel(df);
        dec.initDecoratorPanel(new PositionableLabel("one", editor));
        Assert.assertNotNull("exists", dec);
        df.dispose();
    }

    @Test
    public void testSetTextAttributes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        startEditor();

        DialogRunner dr = new DialogRunner(Color.RED, Color.GREEN, Color.BLUE, "ButtonDone");
        dr.start();
        Thread t = new Thread(() -> {
            ControlPanelEditor.TextAttrDialog dia = _cpe.new TextAttrDialog(_pos, _cpe);
            Assert.assertNotNull("exists", dia);
            dia.setVisible(true);
        });
        t.start();
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return _done;
        }, "Dialog done.");
        new org.netbeans.jemmy.QueueTool().waitEmpty(50);  // allow some time for button push

        PositionablePopupUtil util = _pos.getPopupUtility();
        Assert.assertEquals("font color is red", Color.RED, util.getForeground());
        Assert.assertEquals("background color is green", Color.GREEN, util.getBackground());
        Assert.assertEquals("Border color is blue", Color.BLUE, util.getBorderColor());
        Assert.assertEquals("Margin size", 5, util.getMargin());
        Assert.assertEquals("Border size", 2, util.getBorderSize());
        JUnitUtil.dispose(_cpe);
    }

    @Test
    public void testSetTextAttributesCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        startEditor();

        DialogRunner dr = new DialogRunner(Color.RED, Color.GREEN, Color.BLUE, "ButtonCancel");
        dr.start();
        Thread t = new Thread(() -> {
            ControlPanelEditor.TextAttrDialog dia = _cpe.new TextAttrDialog(_pos, _cpe);
            Assert.assertNotNull("exists", dia);
            dia.setVisible(true);
        });
        t.start();
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return _done;
        }, "Dialog done.");
        new org.netbeans.jemmy.QueueTool().waitEmpty(50);  // allow some time for button push

        PositionablePopupUtil util = _pos.getPopupUtility();
        Assert.assertEquals("font color is cyan", Color.CYAN, util.getForeground());
        Assert.assertEquals("background color is yellow", Color.YELLOW, util.getBackground());
        Assert.assertEquals("Margin size", 0, util.getMargin());
        Assert.assertEquals("Border size", 0, util.getBorderSize());
        JUnitUtil.dispose(_cpe);
    }

    void startEditor() {
        _cpe = new ControlPanelEditor("Fred");
        _pos = new PositionableLabel("Hello There!", _cpe);
        PositionablePopupUtil util =  _pos.getPopupUtility();
        util.setForeground(Color.CYAN);
        util.setBackgroundColor(Color.YELLOW);

        _cpe.putItem(_pos);
        _done = false;
    }

    class DialogRunner extends Thread {
        // constructor for jfo will wait until the dialog is visible
        String _dialogTitle;
        String _buttonTitle;
        int _type;
        Color _colorForeGround;
        Color _colorBackground;
        Color _colorBorder;

        DialogRunner(Color color1, Color color2, Color color3, String buttonTitle) {
            super();
            _colorForeGround = color1;
            _colorBackground = color2;
            _colorBorder = color3;
            _buttonTitle = buttonTitle;
        }

        @Override
        public void run() {
            // constructor for jfo will wait until the dialog is visible
            JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("TextAttributes"));
            JButtonOperator done = new JButtonOperator(jfo, Bundle.getMessage(_buttonTitle));
            ControlPanelEditor.TextAttrDialog dia = (ControlPanelEditor.TextAttrDialog)jfo.getSource();
            DecoratorPanel decorator = dia._decorator;
            PositionablePopupUtil util =  dia._pos.getPopupUtility();
            Assert.assertEquals("foreground color", Color.CYAN, util.getForeground());
            Assert.assertEquals("background color", Color.YELLOW, util.getBackground());

            // default color button is FOREGROUND_BUTTON
            decorator._chooser.setColor(_colorForeGround);

            decorator._borderSpin.setValue(Integer.valueOf(2));
            decorator._selectedButton = DecoratorPanel.BORDERCOLOR_BUTTON;
            // setting border size selects border button
            decorator._chooser.setColor(_colorBorder);

            decorator._marginSpin.setValue(Integer.valueOf(5));
            decorator._selectedButton = DecoratorPanel.BACKGROUND_BUTTON;
            // changing margin of text sixe sets background button
            decorator._chooser.setColor(_colorBackground);

            done.pushNoBlock();
            _done = true;
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DecoratorPanelTest.class);

}
