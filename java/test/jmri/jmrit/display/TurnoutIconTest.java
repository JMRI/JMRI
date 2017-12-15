package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * TurnoutIconTest.java
 *
 * @author	Bob Jacobsen
 */
public class TurnoutIconTest extends PositionableIconTest {

    jmri.jmrit.display.panelEditor.PanelEditor panel = null;

    @Test
    public void testEquals() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame jf = new JFrame("Turnout Icon Test");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        TurnoutIcon to = new TurnoutIcon(panel);
        jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to.setTurnout(new jmri.NamedBeanHandle<>("IT1", turnout));

        TurnoutIcon to2 = new TurnoutIcon(panel);
        turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to2.setTurnout(new jmri.NamedBeanHandle<>("IT1", turnout));

        Assert.assertTrue("identity", to.equals(to));
        Assert.assertFalse("object (not content) equality", to2.equals(to));
        Assert.assertFalse("object (not content) equality commutes", to.equals(to2));
        // close the frame.
        JFrameOperator jfo = new JFrameOperator(jf);
        jfo.requestClose();
    }

    @Override
    @Test
    public void testClone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Editor es = new EditorScaffold();
        TurnoutIcon to = new TurnoutIcon(es);
        jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to.setTurnout(new jmri.NamedBeanHandle<>("IT1", turnout));

        TurnoutIcon to2 = (TurnoutIcon) to.deepClone();

        Assert.assertFalse("clone object (not content) equality", to2.equals(to));

        Assert.assertTrue("class type equality", to2.getClass().equals(to.getClass()));
    }


    @Test
    public void testShow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame jf = new JFrame("Turnout Icon Test");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        TurnoutIcon to = new TurnoutIcon(panel);
        jf.getContentPane().add(to);

        jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to.setTurnout(new jmri.NamedBeanHandle<>("IT1", turnout));

        // test buttons
        JButton throwButton = new JButton("throw");
        throwButton.addActionListener((java.awt.event.ActionEvent e) -> {
            throwButtonPushed();
        });
        jf.getContentPane().add(throwButton);
        JButton closeButton = new JButton("close");
        closeButton.addActionListener((java.awt.event.ActionEvent e) -> {
            closeButtonPushed();
        });
        jf.getContentPane().add(closeButton);
        JButton unknownButton = new JButton("unknown");
        unknownButton.addActionListener((java.awt.event.ActionEvent e) -> {
            unknownButtonPushed();
        });
        jf.getContentPane().add(unknownButton);
        JButton inconsistentButton = new JButton("inconsistent");
        inconsistentButton.addActionListener((java.awt.event.ActionEvent e) -> {
            inconsistentButtonPushed();
        });
        jf.getContentPane().add(inconsistentButton);

        jf.pack();
        jf.setVisible(true);
        // close the frame.
        JFrameOperator jfo = new JFrameOperator(jf);
        jfo.requestClose();

    }

    // animate the visible frame
    public void throwButtonPushed() {
        TurnoutIcon to = new TurnoutIcon(panel);
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
                "KnownState", null, jmri.Turnout.THROWN);
        to.propertyChange(e);
    }

    public void closeButtonPushed() {
        TurnoutIcon to = new TurnoutIcon(panel);
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
                "KnownState", null, jmri.Turnout.CLOSED);
        to.propertyChange(e);
    }

    public void unknownButtonPushed() {
        TurnoutIcon to = new TurnoutIcon(panel);
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
                "KnownState", null, jmri.Turnout.UNKNOWN);
        to.propertyChange(e);
    }

    public void inconsistentButtonPushed() {
        TurnoutIcon to = new TurnoutIcon(panel);
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
                "KnownState", null, 23);
        to.propertyChange(e);
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            panel = new jmri.jmrit.display.panelEditor.PanelEditor("Test TurnoutIcon Panel");
            Editor e = new EditorScaffold();
            p = new TurnoutIcon(e);
        }
    }

    @After
    public void tearDown() {
        // now close panel window
        if (panel != null) {
            java.awt.event.WindowListener[] listeners = panel.getTargetFrame().getWindowListeners();
            for (WindowListener listener : listeners) {
                panel.getTargetFrame().removeWindowListener(listener);
            }

            // close the panel target frame.
            JFrameOperator tf = new JFrameOperator(panel.getTargetFrame());
            tf.requestClose();
        }
        panel = null;
        JUnitUtil.tearDown();
    }
}
