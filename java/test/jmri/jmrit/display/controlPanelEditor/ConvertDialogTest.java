package jmri.jmrit.display.controlPanelEditor;

import jmri.InstanceManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.palette.Bundle;
//import jmri.jmrit.display.IndicatorTrackIcon;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
//import javax.swing.JPanel;
import jmri.util.JUnitUtil;
//import jmri.util.swing.JemmyUtil;

import java.util.ArrayList;
import java.awt.GraphicsEnvironment;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
//import org.netbeans.jemmy.operators.JComponentOperator;
//import org.netbeans.jemmy.*;
/**
 *
 * @author Pete Cressman Copyright (C) 2019   
 */
public class ConvertDialogTest {

    @Test
    public void testCTorConvert() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditor frame = new ControlPanelEditor("ConvertDialogTest");
        OBlock ob1 = InstanceManager.getDefault(OBlockManager.class).createNewOBlock("OB1", "a");
        CircuitBuilder cb = frame.getCircuitBuilder();
        Assert.assertNotNull("exists", cb);
        NamedIcon icon = new NamedIcon("program:resources/icons/smallschematics/tracksegments/block.gif", "track");
        PositionableLabel pos = new PositionableLabel(icon, frame);
        pos.setLocation(200,100);
        frame.putItem(pos);
        ArrayList<Positionable> selections = new ArrayList<>();
        selections.add(pos);
        frame.setSelectionGroup(selections);
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        new Thread(() -> {
            // constructor for d will wait until the dialog is visible
//            System.out.println(" thread running!");
            String title = Bundle.getMessage("IndicatorTrack");
            JDialogOperator d = new JDialogOperator(title);
//            System.out.println(" JDialogOperator found \""+title+"\"!");
//            new org.netbeans.jemmy.QueueTool().waitEmpty(200);
            String label = Bundle.getMessage("updateButton");
            JButtonOperator bo = new JButtonOperator(d, label);
//            System.out.println(" JButtonOperator bo made for \""+label+"\"!");
//            new org.netbeans.jemmy.QueueTool().waitEmpty(200);
            bo.doClick();
//            System.out.println(" JButtonOperator bo Done!");
        }).start();

//        System.out.println(" Open ConvertDialog!");
        ConvertDialog dialog = new ConvertDialog(cb, pos, ob1);
        Assert.assertNotNull("exists",dialog);
//        System.out.println(" ConvertDialog Opened!");

        dialog.dispose();
        frame.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initOBlockManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ConvertDialogTest.class);
}
