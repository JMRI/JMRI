package jmri.jmrit.display;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeEvent;

import javax.swing.JButton;
import javax.swing.JFrame;

import jmri.Turnout;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * TurnoutIconTest.java
 *
 * @author Bob Jacobsen
 */
public class TurnoutIconTest extends PositionableIconTest {

    @Test
    @DisabledIfHeadless
    public void testEquals() {

        JFrame jf = new JFrame("Turnout Icon Test");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        TurnoutIcon to = (TurnoutIcon)p;

        TurnoutIcon to2 = new TurnoutIcon(editor);
        Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to2.setTurnout(new jmri.NamedBeanHandle<>("IT1", turnout));

        assertTrue( to.equals(to), "identity");
        assertFalse( to2.equals(to), "object (not content) equality");
        assertFalse( to.equals(to2), "object (not content) equality commutes");
        // close the frame.
        EditorFrameOperator jfo = new EditorFrameOperator(editor);
        jfo.requestClose();
        jfo.waitClosed();
    }

    @Override
    @Test
    @DisabledIfHeadless
    public void testClone() {

        TurnoutIcon to = (TurnoutIcon)p;

        TurnoutIcon to2 = (TurnoutIcon) to.deepClone();

        assertFalse( to2.equals(to), "clone object (not content) equality");

        assertTrue( to2.getClass().equals(to.getClass()), "class type equality");
    }

    @Override
    @Test
    @DisabledIfHeadless
    public void testShow() {

        JUnitUtil.dispose(editor);
        editor = new jmri.jmrit.display.panelEditor.PanelEditor("Test TurnoutIcon Panel");

        JFrame jf = new JFrame("Turnout Icon Test");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        TurnoutIcon to = new TurnoutIcon(editor);
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
        EditorFrameOperator jfo = new EditorFrameOperator(jf);
        jfo.requestClose();

    }

    // animate the visible frame
    public void throwButtonPushed() {
        TurnoutIcon to = new TurnoutIcon(editor);
        PropertyChangeEvent e = new PropertyChangeEvent(this,
            Turnout.PROPERTY_KNOWN_STATE, null, Turnout.THROWN);
        to.propertyChange(e);
    }

    public void closeButtonPushed() {
        TurnoutIcon to = new TurnoutIcon(editor);
        PropertyChangeEvent e = new PropertyChangeEvent(this,
            Turnout.PROPERTY_KNOWN_STATE, null, Turnout.CLOSED);
        to.propertyChange(e);
    }

    public void unknownButtonPushed() {
        TurnoutIcon to = new TurnoutIcon(editor);
        PropertyChangeEvent e = new PropertyChangeEvent(this,
            Turnout.PROPERTY_KNOWN_STATE, null, Turnout.UNKNOWN);
        to.propertyChange(e);
    }

    public void inconsistentButtonPushed() {
        TurnoutIcon to = new TurnoutIcon(editor);
        PropertyChangeEvent e = new PropertyChangeEvent(this,
            Turnout.PROPERTY_KNOWN_STATE, null, 23);
        to.propertyChange(e);
    }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp(); // creates editor
        TurnoutIcon to = new TurnoutIcon(editor);
        Turnout t = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to.setTurnout(new jmri.NamedBeanHandle<>("IT1", t));
        p = to;
    }

}
