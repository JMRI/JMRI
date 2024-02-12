package jmri.jmrit.display;

import java.awt.Component;

import javax.swing.*;

import jmri.*;
import jmri.implementation.DefaultSignalHead;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.swing.JemmyUtil;
import jmri.util.swing.JmriMouseEvent;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.*;

/**
 * Test the SignalMastIcon.
 *
 * @author Bob Jacobsen Copyright 2009
 * @author Steve Young Copyright 2024
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class SignalMastIconTest extends PositionableIconTest {

    private SignalMast s = null;
    private SignalMastIcon to = null;

    @Test
    public void testShowText() {
        // this one is for Layout editor, which for now
        // is still in text form.
        JFrame jf = new JFrame("SignalMast Icon Text Test");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        jf.getContentPane().add(new JLabel("Should say Approach: "));
        jf.getContentPane().add(to);

        s.setAspect("Clear");
        s.setAspect("Approach");

        jf.pack();
        jf.setVisible(true);

        // close
        JUnitUtil.dispose(jf);

    }

    @Test
    public void testShowIcon() {

        JFrame jf = new JFrame("SignalMastIcon Icon Test");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        to = new SignalMastIcon(editor);
        to.setShowAutoText(false);

        jf.getContentPane().add(new JLabel("Should be yellow/red: "));
        jf.getContentPane().add(to);

        s = InstanceManager.getDefault(jmri.SignalMastManager.class)
                .provideSignalMast("IF$shsm:basic:two-searchlight:IH2:IH3");

        s.setAspect("Clear");

        to.setSignalMast(s.getSystemName());

        s.setAspect("Clear");
        s.setAspect("Approach");

        jf.pack();
        jf.setVisible(true);

        // close
        JUnitUtil.dispose(jf);
    }

    @Test
    public void testSelectClickSensorCancel() throws JmriException {
        JFrame jf = new JFrame("SignalMastIcon testSelectClickSensorCancel");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        to = new SignalMastIcon(editor);
        to.setShowAutoText(false);

        jf.getContentPane().add(to);

        Sensor s1 = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        Sensor s2 = InstanceManager.getDefault(SensorManager.class).provide("IS2");
        s2.setUserName("Sensor 2 UName");
        s1.setKnownState(Sensor.INACTIVE);
        s2.setKnownState(Sensor.INACTIVE);

        s = InstanceManager.getDefault(SignalMastManager.class)
                .provideSignalMast("IF$vsm:BR-2003:2-h($0001)");       
        s.setAspect("Danger");

        to.setSignalMast(s.getSystemName());

        jf.pack();
        ThreadingUtil.runOnGUI( () -> jf.setVisible(true));

        JPopupMenu popup = new JPopupMenu();
        Assertions.assertTrue(to.showPopUp(popup));
        ThreadingUtil.runOnGUI( () -> popup.show( (Component)to, 0, 0 ) );

        Thread selectSensor = JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("SelectSensActive"), 
                Bundle.getMessage("ButtonCancel"));

        // Access popup Menu
        JPopupMenuOperator jpo = new JPopupMenuOperator();
        Assertions.assertNotNull(jpo);
        JMenuItemOperator jmo = new JMenuItemOperator(jpo,Bundle.getMessage("WhenClicked"));
        jmo.doClick();

        JMenuOperator whenClickedOper = new JMenuOperator(jpo);
        JPopupMenuOperator hh = new JPopupMenuOperator(((JPopupMenu)whenClickedOper.getSubElements()[0]));
        Assertions.assertNotNull(hh);
        new JMenuItemOperator(hh,Bundle.getMessage("ActivateSensor","")).doClick();

        JUnitUtil.waitFor( () -> !selectSensor.isAlive(), "Cancel dialog finished");

        Assertions.assertEquals(0, to.getClickMode());

        JFrameOperator jfo = new JFrameOperator(jf.getTitle());
        Assertions.assertNotNull(jf);

        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testSelectClickSensorSelected() throws JmriException {
        JFrame jf = new JFrame("SignalMastIcon testSelectClickSensorSelected");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        to = new SignalMastIcon(editor);
        to.setShowAutoText(false);
        jf.getContentPane().add(to);

        Sensor s1 = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        Sensor s2 = InstanceManager.getDefault(SensorManager.class).provide("IS2");
        s2.setUserName("Sensor 2 UName");
        s1.setKnownState(Sensor.INACTIVE);
        s2.setKnownState(Sensor.INACTIVE);

        s = InstanceManager.getDefault(SignalMastManager.class)
                .provideSignalMast("IF$vsm:BR-2003:2-h($0001)");       
        s.setAspect("Danger");

        to.setSignalMast(s.getSystemName());

        jf.pack();
        ThreadingUtil.runOnGUI( () -> jf.setVisible(true));

        JPopupMenu popup = new JPopupMenu();
        to.showPopUp(popup);
        ThreadingUtil.runOnGUI( () -> popup.show( (Component)to, 0, 0 ) );

        Thread selectSensor = new Thread(() -> {
            JDialogOperator jdo = new JDialogOperator( Bundle.getMessage("SelectSensActive") );
            JComboBoxOperator jcbo = new JComboBoxOperator(jdo);
            jcbo.setSelectedItem(s1);
            new JButtonOperator(jdo,"OK").doClick();
        });
        selectSensor.setName("JDO Sensor Select Thread");
        selectSensor.start();

        // Access popup Menu
        JPopupMenuOperator jpo = new JPopupMenuOperator();
        Assertions.assertNotNull(jpo);
        JMenuItemOperator jmo = new JMenuItemOperator(jpo,Bundle.getMessage("WhenClicked"));
        jmo.doClick();

        JMenuOperator whenClickedOper = new JMenuOperator(jpo);
        JPopupMenuOperator hh = new JPopupMenuOperator(((JPopupMenu)whenClickedOper.getSubElements()[0]));
        Assertions.assertNotNull(hh);
        new JMenuItemOperator(hh,Bundle.getMessage("ActivateSensor","")).doClick();

        JUnitUtil.waitFor( () -> !selectSensor.isAlive(), "Select dialog finished");
        JUnitUtil.waitFor( () -> to.getClickMode()==3, "ClickMode updated");
        Sensor cs = to.getClickSensor();
        Assertions.assertNotNull(cs);
        Assertions.assertEquals("IS1", cs.getDisplayName());
        Assertions.assertNotEquals(Sensor.ACTIVE, s1.getCommandedState());

        JmriMouseEvent e = new JmriMouseEvent((Component)to,
                    JmriMouseEvent.MOUSE_CLICKED,
                    0, // time
                    0, // modifiers
                    0, 0, // this component expects global positions for some reason
                    1, // one click
                    false // not a popup
            );
        to.doMouseClicked(e);
        Assertions.assertEquals(Sensor.ACTIVE, s1.getCommandedState());

        JFrameOperator jfo = new JFrameOperator(jf.getTitle());
        Assertions.assertNotNull(jf);

        jfo.requestClose();
        jfo.waitClosed();

    }

    @Test
    public void testRightClickSensor() throws JmriException {

        JFrame jf = new JFrame("SignalMastIcon testSelectClickSensorSelected");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());
        to = new SignalMastIcon(editor);
        to.setShowAutoText(false);
        jf.getContentPane().add(to);
        jf.pack();
        ThreadingUtil.runOnGUI( () -> jf.setVisible(true) );

        Sensor s1 = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        Sensor s2 = InstanceManager.getDefault(SensorManager.class).provide("IS2");
        s2.setUserName("Sensor 2 UName");
        s1.setKnownState(Sensor.INACTIVE);
        s2.setKnownState(Sensor.INACTIVE);

        to.setControlClickMode(1);
        Assertions.assertEquals(1, to.getControlClickMode());
        to.setControlClickSensor(s2.getSystemName());
        Assertions.assertEquals(s2, to.getControlClickSensor());
        Assertions.assertNotEquals(Sensor.ACTIVE, s2.getCommandedState());

        to.setEditable(false);
        Assertions.assertFalse(to.showPopUp(null),"does not display popup");
        Assertions.assertEquals(Sensor.ACTIVE, s2.getCommandedState());

        JFrameOperator jfo = new JFrameOperator(jf.getTitle());
        Assertions.assertNotNull(jf);

        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testEditIconFromPopUp() throws JmriException {
        JFrame jf = new JFrame("SignalMastIcon testEditIconFromPopUp");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        to = new SignalMastIcon(editor);
        to.setShowAutoText(false);
        jf.getContentPane().add(to);

        s = InstanceManager.getDefault(SignalMastManager.class)
                .provideSignalMast("IF$vsm:BR-2003:2-h($0001)");       
        s.setAspect("Danger");

        to.setSignalMast(s.getSystemName());

        Block b1 = InstanceManager.getDefault(BlockManager.class).provideBlock("IB12");
        b1.setUserName("Block 1");
        Section section1 = InstanceManager.getDefault(SectionManager.class).createNewSection("TS1");
        section1.addBlock(b1);
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).enableAdvancedRouting(true);

        jf.pack();
        ThreadingUtil.runOnGUI( () -> jf.setVisible(true));

        JPopupMenu popup = new JPopupMenu();
        Assertions.assertTrue(to.setEditItemMenu(popup));
        ThreadingUtil.runOnGUI( () -> popup.show( (Component)to, 0, 0 ) );

        JPopupMenuOperator jpo = new JPopupMenuOperator();
        Assertions.assertNotNull(jpo);

        JMenuItemOperator jmo = new JMenuItemOperator(jpo,"Edit Signal Mast Icon...");
        jmo.doClick();

        JFrameOperator jfo = new JFrameOperator(jmo.getText());
        Assertions.assertNotNull(jfo);

        jfo.requestClose();
        jfo.waitClosed();

        jfo = new JFrameOperator(jf.getTitle());
        Assertions.assertNotNull(jf);

        jfo.requestClose();
        jfo.waitClosed();
    }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        JUnitUtil.initInternalSignalHeadManager();

        editor = new EditorScaffold();
        p = new SignalMastIcon(editor);
        to = new SignalMastIcon(editor);
        to.setShowAutoText(true);

        // reset instance manager & create test heads
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new DefaultSignalHead("IH1") {
                    @Override
                    protected void updateOutput() {
                    }
                });
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new DefaultSignalHead("IH2") {
                    @Override
                    protected void updateOutput() {
                    }
                });
        InstanceManager.getDefault(SignalHeadManager.class).register(
                new DefaultSignalHead("IH3") {
                    @Override
                    protected void updateOutput() {
                    }
                });

        s = InstanceManager.getDefault(SignalMastManager.class)
                .provideSignalMast("IF$shsm:basic:one-searchlight:IH1");

        to.setSignalMast(new NamedBeanHandle<>(s.getSystemName(), s));

    }

    @AfterEach
    @Override
    public void tearDown() {
        to.dispose();
        to = null;
        s.dispose();
        s = null;
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SignalMastIconTest.class);
}
