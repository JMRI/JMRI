package jmri.jmrit.display.controlPanelEditor;

import jmri.InstanceManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.palette.Bundle;
import jmri.jmrit.display.IndicatorTrackIcon;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import javax.swing.JPanel;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import java.awt.GraphicsEnvironment;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.*;
/**
 *
 * @author Pete Cressman Copyright (C) 2019   
 */
public class ConvertDialogTest {

    @Test
    @org.junit.Ignore("Cannot get button pushed!")
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

        ConvertDialog dialog = new ConvertDialog(cb, pos, ob1);
        Assert.assertNotNull("exists",dialog);

        JDialogOperator jdo = new JDialogOperator(dialog);
/*        ComponentSearcher cs = new ComponentSearcher(dialog);
//        cs.findComponent(chooser);
//        JComponentOperator jco = new JComponentOperator(jdo, new ComponentChooser());
        java.awt.Component[] comps = dialog.getComponents();
        for (int i=0; i<comps.length; i++) {
            if (comps[i] instanceof JPanel) {
                JComponentOperator jco = new JComponentOperator(comps[i]);
            }
        }*/
        JButtonOperator jbo = new JButtonOperator(jdo, Bundle.getMessage("updateButton"));
        System.out.println(" JButtonOperator jbo Done!");
        jbo.doClick();
     
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
        JUnitUtil.initShutDownManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ConvertDialogTest.class);
}
