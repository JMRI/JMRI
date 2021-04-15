package jmri.jmrit.display.controlPanelEditor;

import jmri.InstanceManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableLabel;
//import jmri.jmrit.display.IndicatorTrackIcon;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
//import javax.swing.JPanel;
import jmri.util.JUnitUtil;
//import jmri.util.swing.JemmyUtil;

import java.util.ArrayList;
import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
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
//            String title = Bundle.getMessage("IndicatorTrack");
            JDialogOperator d = new JDialogOperator("Indicator Track");
//            String label = Bundle.getMessage("convert");
            JButtonOperator bo = new JButtonOperator(d, "Convert Icon");
            bo.doClick();
        }).start();

        ConvertDialog dialog = new ConvertDialog(cb, pos, ob1);
        Assert.assertNotNull("exists",dialog);

        dialog.dispose();
        frame.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initOBlockManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ConvertDialogTest.class);
}
