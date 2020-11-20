package jmri.jmrit.beantable.oblock;

import jmri.BeanSetting;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OPath;
import jmri.util.JUnitUtil;

import jmri.util.gui.GuiLafPreferencesManager;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.netbeans.jemmy.operators.JFrameOperator;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Egbert Broerse Copyright (C) 2020
 */
public class PathTurnoutTableModelTest {

    @Test
    public void testCTor() {
        PathTurnoutTableModel t = new PathTurnoutTableModel();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testPathCTor() {
        OBlock ob1 = new OBlock("OB1");
        OPath p1 = new OPath(ob1, "OP1");
        PathTurnoutTableModel t = new PathTurnoutTableModel(p1);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testBlockPathCTor() {
        OBlock ob1 = new OBlock("OB1");
        OPath p1 = new OPath(ob1, "OP1");
        TableFrames tf = new TableFrames();
        TableFrames.PathTurnoutFrame ptf = tf.makePathTurnoutFrame(ob1, "OP1");
        PathTurnoutTableModel pttm = new PathTurnoutTableModel(p1, ptf);
        Assert.assertNotNull("exists", pttm);
    }

    @Test
    public void testBlockPathTurnoutPanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Turnout t1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        OBlock ob1 = new OBlock("OB1");
        OPath p1 = new OPath(ob1, "OP1");
        BeanSetting bs = new BeanSetting(t1, "IT1", Turnout.CLOSED);
        p1.addSetting(bs);
        TableFrames tf = new TableFrames();
        TableFrames.PathTurnoutFrame ptf = tf.makePathTurnoutFrame(ob1, "OP1");
        PathTurnoutTableModel pttm = new PathTurnoutTableModel(p1, ptf);
        JTable pttt = new JTable(pttm);
        JFrame pttf = new JFrame("Test");
        pttf.add(pttt);
        pttf.setSize(new Dimension(300, 100));
        pttf.setVisible(true);
        Assert.assertNotNull("PtToTabledisplay exists", pttf);
//        // Find Add Turnout pane by name (copied from TurnoutTableWindowTest)
        JFrameOperator afo = new JFrameOperator("Test");
//        // Ask to close Add pane
        afo.requestClose();

        tf.openPathTurnoutEditPane(tf.makePathTurnoutName("OB1", "OP1"));
//        afo = new JFrameOperator(Bundle.getMessage("TitlePathTurnoutTable", "OB1", "OP1"));
//        afo.requestClose();

        tf.addTurnoutPane(p1, pttm);
        afo = new JFrameOperator(Bundle.getMessage("NewTurnoutTitle", "OP1"));
        afo.requestClose();

        pttm.removeListener();
        // test?
    }

    @Test
    public void testBlockPathTurnoutCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Turnout t1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        OBlock ob1 = new OBlock("OB1");
        OPath p1 = new OPath(ob1, "OP1");
        BeanSetting bs = new BeanSetting(t1, "IT1", Turnout.CLOSED);
        p1.addSetting(bs);
        TableFrames tf = new TableFrames();
        TableFrames.PathTurnoutFrame ptf = tf.makePathTurnoutFrame(ob1, "OP1");
        PathTurnoutTableModel pttm = new PathTurnoutTableModel(p1, ptf);
        Assert.assertNotNull("exists", pttm);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // use _tabbed interface
        InstanceManager.getDefault(GuiLafPreferencesManager.class).setOblockEditTabbed(true);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PathTurnoutTableModelTest.class);

}
