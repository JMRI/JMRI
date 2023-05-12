package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.jmrit.logixng.GlobalVariable;
import jmri.jmrit.logixng.GlobalVariableManager;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Bob Jacobsen     Copyright 2009
 * @author Daniel Bergqvist Copyright (C) 2022
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class GlobalVariableSpinnerIconTest extends PositionableJPanelTest {

    GlobalVariableSpinnerIcon tos1 = null;
    GlobalVariableSpinnerIcon tos2 = null;
    GlobalVariableSpinnerIcon tos3 = null;
    GlobalVariableSpinnerIcon toi1 = null;
    GlobalVariableSpinnerIcon toi2 = null;
    GlobalVariableSpinnerIcon toi3 = null;

    @Override
    @Test
    public void testShow() {

        JmriJFrame jf = new JmriJFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        tos1 = new GlobalVariableSpinnerIcon(editor);
        jf.getContentPane().add(tos1);
        tos2 = new GlobalVariableSpinnerIcon(editor);
        jf.getContentPane().add(tos2);
        toi1 = new GlobalVariableSpinnerIcon(editor);
        jf.getContentPane().add(toi1);
        toi2 = new GlobalVariableSpinnerIcon(editor);
        jf.getContentPane().add(toi2);

        InstanceManager.getDefault().clearAll();
        JUnitUtil.initDefaultUserMessagePreferences();

        jmri.InstanceManager.getDefault(GlobalVariableManager.class).createGlobalVariable("MyVar");
        jmri.InstanceManager.getDefault(GlobalVariableManager.class).createGlobalVariable("MyOtherVar");

        tos1.setGlobalVariable("MyVar");
        tos2.setGlobalVariable("MyVar");
        GlobalVariable im1 = InstanceManager.getDefault(GlobalVariableManager.class).getGlobalVariable("MyVar");
        Assert.assertNotNull(im1);
        im1.setValue("4");

        toi1.setGlobalVariable("MyOtherVar");
        toi2.setGlobalVariable("MyOtherVar");
        GlobalVariable im2 = InstanceManager.getDefault(GlobalVariableManager.class).getGlobalVariable("MyOtherVar");
        Assert.assertNotNull(im2);
        im2.setValue(10);

        tos3 = new GlobalVariableSpinnerIcon(editor);
        jf.getContentPane().add(tos3);
        toi3 = new GlobalVariableSpinnerIcon(editor);
        jf.getContentPane().add(toi3);
        tos3.setGlobalVariable("MyVar");
        toi3.setGlobalVariable("MyOtherVar");
        im1.setValue(11.58F);
        im2.setValue(0.89);
        tos1.setGlobalVariable("MyVar");
        Assert.assertEquals("Spinner 1", "12", tos1.getValue());
        tos2.setGlobalVariable("MyOtherVar");
        Assert.assertEquals("Spinner 2", "12", tos1.getValue());

        jf.pack();
        jf.setVisible(true);

        if (!System.getProperty("jmri.demo", "false").equals("false")) {
            jf.setVisible(false);
            jf.dispose();
        }
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        jmri.InstanceManager.getDefault(GlobalVariableManager.class).createGlobalVariable("MyVar");
        jmri.InstanceManager.getDefault(GlobalVariableManager.class).createGlobalVariable("MyOtherVar");
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);

        editor = new jmri.jmrit.display.panelEditor.PanelEditor("Test GlobalVariableSpinnerIcon Panel");
        p=tos1 = new GlobalVariableSpinnerIcon(editor);
        tos1.setGlobalVariable("MyVar");

    }

    @Override
    @AfterEach
    public void tearDown() {
        tos1 = null;
        tos2 = null;
        tos3 = null;
        toi1 = null;
        toi2 = null;
        toi3 = null;
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutIconTest.class);
}
