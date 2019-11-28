package jmri.jmrit.operations.setup;

import java.awt.GraphicsEnvironment;
import javax.swing.JComboBox;
import jmri.jmrit.display.LocoIcon;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;

/**
 * Tests for OperationsSetupFrame
 *
 * @author Dan Boudreau Copyright (C) 2009
 * @author Paul Bender Copyright (C) 2017
 */
public class OperationsSetupFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OperationsSetupFrame t = new OperationsSetupFrame();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testDirectionCheckBoxes() {
        // it may be possible to make this a headless test by only initializing the panel, not the frame
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OperationsSetupFrame f = new OperationsSetupFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents();

        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("TitleOperationsSetup"));

        // first confirm that setup has all directions selected
        Assert.assertEquals("All directions selected", Setup.EAST + Setup.WEST + Setup.NORTH + Setup.SOUTH,
                Setup.getTrainDirection());

        JCheckBoxOperator northCheckBoxOperator = new JCheckBoxOperator(jfo,Bundle.getMessage("northsouth"));
        JCheckBoxOperator eastCheckBoxOperator = new JCheckBoxOperator(jfo,Bundle.getMessage("eastwest"));


        // both east/west and north/south checkboxes should be set
        Assert.assertTrue("North selected", northCheckBoxOperator.isSelected());
        Assert.assertTrue("East selected", eastCheckBoxOperator.isSelected());

        northCheckBoxOperator.push();
        Assert.assertFalse("North deselected", northCheckBoxOperator.isSelected());
        Assert.assertTrue("East selected", eastCheckBoxOperator.isSelected());

        eastCheckBoxOperator.push();
        Assert.assertTrue("North selected", northCheckBoxOperator.isSelected());
        Assert.assertFalse("East deselected", eastCheckBoxOperator.isSelected());

        eastCheckBoxOperator.push();
        Assert.assertTrue("North selected", northCheckBoxOperator.isSelected());
        Assert.assertTrue("East selected", eastCheckBoxOperator.isSelected());

        // done
        JUnitUtil.dispose(f);
    }

    @Test
    public void testSetupFrameWrite() {
        // it may be possible to make this a headless test by only initializing the panel, not the frame
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // force creation of backup
        Setup.setCarTypes(Setup.AAR);

        OperationsSetupFrame f = new OperationsSetupFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents();
        OperationsSetupPanel p = (OperationsSetupPanel) f.getContentPane();

        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("TitleOperationsSetup"));


        p.railroadNameTextField.setText("Test Railroad Name");
        p.maxLengthTextField.setText("1234");
        p.maxEngineSizeTextField.setText("6");
        p.switchTimeTextField.setText("3");
        p.travelTimeTextField.setText("4");

        (new JRadioButtonOperator(jfo,"HO",1)).push(); // Match 0 is HOn3.
        (new JRadioButtonOperator(jfo,Bundle.getMessage("Descriptive"))).push();

        p.panelTextField.setText("Test Panel Name");

        ((JComboBox<?>)(new JLabelOperator(jfo,Bundle.getMessage("IconNorth")).getLabelFor())).setSelectedItem(LocoIcon.RED);
        ((JComboBox<?>)(new JLabelOperator(jfo,Bundle.getMessage("IconEast")).getLabelFor())).setSelectedItem(LocoIcon.BLUE);
        ((JComboBox<?>)(new JLabelOperator(jfo,Bundle.getMessage("IconWest")).getLabelFor())).setSelectedItem(LocoIcon.WHITE);
        ((JComboBox<?>)(new JLabelOperator(jfo,Bundle.getMessage("IconSouth")).getLabelFor())).setSelectedItem(LocoIcon.GREEN);
        ((JComboBox<?>)(new JLabelOperator(jfo,Bundle.getMessage("IconTerminate")).getLabelFor())).setSelectedItem(LocoIcon.GRAY);
        ((JComboBox<?>)(new JLabelOperator(jfo,Bundle.getMessage("IconLocal")).getLabelFor())).setSelectedItem(LocoIcon.YELLOW);

        (new JButtonOperator(jfo,Bundle.getMessage("ButtonSave"))).push();

        // dialog window should appear regarding train lengths
        JemmyUtil.pressDialogButton(f,java.text.MessageFormat.format(
                    Bundle.getMessage("MaxTrainLengthIncreased"), new Object[]{1234,"feet"}), Bundle.getMessage("ButtonOK"));
        // dialog window should appear regarding railroad name
        /*pressDialogButton(f,java.text.MessageFormat.format(Bundle
                    .getMessage("ChangeRailroadName"), new Object[]{"My Jmri Railroad", "Test Railroad Name"}) ,Bundle.getMessage("ButtonNo"));
        // done*/
        JUnitUtil.dispose(f);
        jfo.dispose();

        // it may be possible to make this a headless test by only initializing the panel, not the frame
        OperationsSetupFrame frameRead = new OperationsSetupFrame();
        frameRead.setLocation(0, 0); // entire panel must be visible for tests to work properly
        frameRead.initComponents();
        OperationsSetupPanel panelRead = (OperationsSetupPanel) frameRead.getContentPane();

        JFrameOperator jfo2 = new JFrameOperator(Bundle.getMessage("TitleOperationsSetup"));

        Assert.assertEquals("railroad name", "Test Railroad Name", panelRead.railroadNameTextField.getText());
        Assert.assertEquals("max length", "1234", panelRead.maxLengthTextField.getText());
        Assert.assertEquals("max engines", "6", panelRead.maxEngineSizeTextField.getText());
        Assert.assertEquals("switch time", "3", panelRead.switchTimeTextField.getText());
        Assert.assertEquals("travel time", "4", panelRead.travelTimeTextField.getText());
        // Assert.assertEquals("owner", "Bob J", f.ownerTextField.getText());


        Assert.assertTrue("HO scale", (new JRadioButtonOperator(jfo2,"HO",1)).isSelected());
        Assert.assertFalse("N scale", (new JRadioButtonOperator(jfo2,"N")).isSelected());
        Assert.assertFalse("Z scale", (new JRadioButtonOperator(jfo2,"Z")).isSelected());
        Assert.assertFalse("TT scale", (new JRadioButtonOperator(jfo2,"TT")).isSelected());
        Assert.assertFalse("HOn3 scale", (new JRadioButtonOperator(jfo2,"HOn3")).isSelected());
        Assert.assertFalse("OO scale", (new JRadioButtonOperator(jfo2,"OO")).isSelected());
        Assert.assertFalse("Sn3 scale", (new JRadioButtonOperator(jfo2,"Sn3")).isSelected());
        Assert.assertFalse("S scale", (new JRadioButtonOperator(jfo2,"S",1)).isSelected());
        Assert.assertFalse("On3 scale", (new JRadioButtonOperator(jfo2,"On3")).isSelected());
        Assert.assertFalse("O scale", (new JRadioButtonOperator(jfo2,"O",1)).isSelected());
        Assert.assertFalse("G scale", (new JRadioButtonOperator(jfo2,"G")).isSelected());

        Assert.assertTrue("descriptive", (new JRadioButtonOperator(jfo2,Bundle.getMessage("Descriptive"))).isSelected());
        Assert.assertFalse("AAR", (new JRadioButtonOperator(jfo2,Bundle.getMessage("AAR"))).isSelected());

        Assert.assertEquals("panel name", "Test Panel Name", panelRead.panelTextField.getText());

        Assert.assertEquals("east color", LocoIcon.RED, ((JComboBox<?>)(new JLabelOperator(jfo2,Bundle.getMessage("IconNorth")).getLabelFor())).getSelectedItem());
        Assert.assertEquals("west color", LocoIcon.BLUE, ((JComboBox<?>)(new JLabelOperator(jfo2,Bundle.getMessage("IconEast")).getLabelFor())).getSelectedItem());
        Assert.assertEquals("north color", LocoIcon.WHITE, ((JComboBox<?>)(new JLabelOperator(jfo2,Bundle.getMessage("IconWest")).getLabelFor())).getSelectedItem());
        Assert.assertEquals("south color", LocoIcon.GREEN, ((JComboBox<?>)(new JLabelOperator(jfo2,Bundle.getMessage("IconSouth")).getLabelFor())).getSelectedItem());
        Assert.assertEquals("terminate color", LocoIcon.GRAY, ((JComboBox<?>)(new JLabelOperator(jfo2,Bundle.getMessage("IconTerminate")).getLabelFor())).getSelectedItem());
        Assert.assertEquals("local color", LocoIcon.YELLOW, ((JComboBox<?>)(new JLabelOperator(jfo2,Bundle.getMessage("IconLocal")).getLabelFor())).getSelectedItem());
        // done
        JUnitUtil.dispose(frameRead);
    }

    // private final static Logger log = LoggerFactory.getLogger(OperationsSetupFrameTest.class);

}
