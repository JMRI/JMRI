package jmri.jmrit.beantable;

import java.awt.GraphicsEnvironment;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.SignalGroup;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.Turnout;
import jmri.jmrit.beantable.SignalGroupTableAction;
import jmri.jmrit.beantable.SignalGroupSubTableAction;
import jmri.util.JmriJFrame;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Tests for the jmri.jmrit.beantable.SignalGroupTableAction class
 *
 * @author	Egbert Broerse Copyright 2017
 */
public class SignalGroupTableActionTest {

    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Action a = new SignalGroupTableAction();
        Assert.assertNotNull(a);
    }

    @Test
    public void testInvoke() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new SignalGroupTableAction().actionPerformed(null);
        JFrame f = JFrameOperator.findJFrame(Bundle.getMessage("TitleSignalGroupTable"), true, true);
        Assert.assertNotNull("found frame", f);
        f.dispose();
    }

    private SignalGroupTableAction _sGroupTable;

    @Test
    public void testAdd() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // create a signal mast
        SignalMast sm = new jmri.implementation.VirtualSignalMast("IF$vsm:AAR-1946:CPL($0002)", "VM1");
        // create a Turnout
        Turnout it1 = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        // create a signal head
        jmri.implementation.SingleTurnoutSignalHead sh
                = new jmri.implementation.SingleTurnoutSignalHead("IH1",
                new jmri.NamedBeanHandle<Turnout>("IT1", it1),
                SignalHead.LUNAR, SignalHead.DARK); // on state + off state
        // open Signal Group Table
        _sGroupTable = new SignalGroupTableAction();
        _sGroupTable.addPressed(null);
        JFrame af = JFrameOperator.waitJFrame(Bundle.getMessage("AddSignalGroup"), true, true);
        Assert.assertNotNull("found Add frame", af);
        // create a new signal group
        _sGroupTable._userName.setText("TestGroup");
        Assert.assertEquals("user name", "TestGroup", _sGroupTable._userName.getText());
        _sGroupTable._systemName.setText("R1");
        Assert.assertEquals("system name", "R1", _sGroupTable._systemName.getText());
        _sGroupTable.mainSignal.setSelectedBeanByName("VM1");
        SignalGroup g = _sGroupTable.checkNamesOK();
        _sGroupTable.setValidSignalMastAspects();
        // add the head to the group:
        //g.addSignalHead(sh);

        // causes NPE when bypassing the GUI to open an Edit Head pane:
        // open Edit head pane
        //SignalGroupSubTableAction editSignalHead = new SignalGroupSubTableAction();
        //editSignalHead.editHead(g, "IH1");
        //editSignalHead.cancelSubPressed(null); // close edit head pane

        _sGroupTable.cancelPressed(null); // calling updatePressed() complains about duplicate group name
        // clean up
        af.dispose();
        g.dispose();
        _sGroupTable.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
