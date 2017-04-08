package jmri.jmrit.operations.setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GraphicsEnvironment;
import jmri.jmrit.display.LocoIcon;
import jmri.jmrit.operations.OperationsSwingTestCase;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.util.NameComponentChooser;
import javax.swing.JComboBox;

/**
 * Tests for the OptionFrame class
 *
 * @author Dan Boudreau Copyright (C) 2009
 * @author Paul Bender Copyright (C) 2017	
 */
public class OptionFrameTest extends OperationsSwingTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OptionFrame t = new OptionFrame();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testOptionFrameWrite() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OptionFrame f = new OptionFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents();
        OptionPanel p = (OptionPanel) f.getContentPane();

        // confirm defaults
        Assert.assertTrue("build normal", p.buildNormal.isSelected());
        Assert.assertFalse("build aggressive", p.buildAggressive.isSelected());
        Assert.assertFalse("local", p.localSpurCheckBox.isSelected());
        Assert.assertFalse("interchange", p.localInterchangeCheckBox.isSelected());
        Assert.assertFalse("yard", p.localYardCheckBox.isSelected());
        Assert.assertFalse("rfid", p.rfidCheckBox.isSelected());
        Assert.assertFalse("car logger", p.carLoggerCheckBox.isSelected());
        Assert.assertFalse("engine logger", p.engineLoggerCheckBox.isSelected());
        Assert.assertTrue("router", p.routerCheckBox.isSelected());

        enterClickAndLeave(p.buildAggressive);
        Assert.assertFalse("build normal", p.buildNormal.isSelected());
        Assert.assertTrue("build aggressive", p.buildAggressive.isSelected());

        enterClickAndLeave(p.localSpurCheckBox);
        Assert.assertTrue("local", p.localSpurCheckBox.isSelected());

        enterClickAndLeave(p.localInterchangeCheckBox);
        Assert.assertTrue("interchange", p.localInterchangeCheckBox.isSelected());

        enterClickAndLeave(p.localYardCheckBox);
        Assert.assertTrue("yard", p.localYardCheckBox.isSelected());

        //        enterClickAndLeave(p.rfidCheckBox);
        // use doClick() in case the checkbox isn't visible due to scrollbars.
        p.rfidCheckBox.doClick();
        Assert.assertTrue("rfid", p.rfidCheckBox.isSelected());

        enterClickAndLeave(p.carLoggerCheckBox);
        Assert.assertTrue("car logger", p.carLoggerCheckBox.isSelected());

        enterClickAndLeave(p.engineLoggerCheckBox);
        Assert.assertTrue("engine logger", p.engineLoggerCheckBox.isSelected());

        enterClickAndLeave(p.routerCheckBox);
        Assert.assertFalse("router", p.routerCheckBox.isSelected());

        enterClickAndLeave(p.saveButton);
        // done
        f.dispose();

        f = new OptionFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents();
        p = (OptionPanel) f.getContentPane();

        Assert.assertFalse("build normal", p.buildNormal.isSelected());
        Assert.assertTrue("build aggressive", p.buildAggressive.isSelected());
        Assert.assertTrue("local", p.localSpurCheckBox.isSelected());
        Assert.assertTrue("interchange", p.localInterchangeCheckBox.isSelected());
        Assert.assertTrue("yard", p.localYardCheckBox.isSelected());
        Assert.assertTrue("rfid", p.rfidCheckBox.isSelected());
        Assert.assertTrue("car logger", p.carLoggerCheckBox.isSelected());
        Assert.assertTrue("engine logger", p.engineLoggerCheckBox.isSelected());
        Assert.assertFalse("router", p.routerCheckBox.isSelected());

        // done
        f.dispose();
    }

    // Ensure minimal setup for log4J
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        new Setup();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(OptionFrameTest.class.getName());

}
