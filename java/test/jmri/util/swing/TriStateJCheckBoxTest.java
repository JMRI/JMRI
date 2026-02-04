package jmri.util.swing;

import javax.swing.JCheckBox;

import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

import org.netbeans.jemmy.operators.*;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit Tests for TriStateJCheckBox.
 * 
 * @author Steve Young Copyright (c) 2021
 */
public class TriStateJCheckBoxTest {

    @Test
    public void testCTor() {
        TriStateJCheckBox t = new TriStateJCheckBox();
        assertNotNull( t, "exists");
    }

    @Test
    public void testInitialState() {
        TriStateJCheckBox t = new TriStateJCheckBox();
        assertFalse( t.isSelected(), "start not selected");
        assertEquals( TriStateJCheckBox.State.UNCHECKED, t.getState(), "start unchecked");
    }

    @Test
    public void testSetGetState() {

        TriStateJCheckBox t = new TriStateJCheckBox();

        t.setState(TriStateJCheckBox.State.CHECKED);
        assertTrue( t.isSelected(), "selected");
        assertEquals( TriStateJCheckBox.State.CHECKED, t.getState(), "checked");

        t.setState(TriStateJCheckBox.State.UNCHECKED);
        assertFalse( t.isSelected(), "not selected");
        assertEquals( TriStateJCheckBox.State.UNCHECKED, t.getState(), "unchecked");

        t.setState(TriStateJCheckBox.State.PARTIAL);
        assertFalse( t.isSelected(), "not selected partial");
        assertEquals( TriStateJCheckBox.State.PARTIAL, t.getState(), "partial");

    }

    @Test
    public void testSetStateFromBoolean() {

        TriStateJCheckBox t = new TriStateJCheckBox();

        t.setState(new boolean[]{true});
        assertTrue( t.isSelected(), "bool selected");
        assertEquals( TriStateJCheckBox.State.CHECKED, t.getState(), "bool checked");

        t.setState(new boolean[]{false});
        assertFalse( t.isSelected(), "bool not selected");
        assertEquals( TriStateJCheckBox.State.UNCHECKED, t.getState(), "bool unchecked");

        t.setState(new boolean[]{true, false});
        assertFalse( t.isSelected(), "bool not selected partial");
        assertEquals( TriStateJCheckBox.State.PARTIAL, t.getState(), "bool partial");

        t.setState(new boolean[]{false, true, false});
        assertFalse( t.isSelected(), "bool not selected partial");
        assertEquals( TriStateJCheckBox.State.PARTIAL, t.getState(), "bool partial");

        t.setState(new boolean[]{true,true,true,true,true,true,true,true,true,true,true,true,true,true});
        assertTrue( t.isSelected(), "bool selected");
        assertEquals( TriStateJCheckBox.State.CHECKED, t.getState(), "bool checked");

        t.setState(new boolean[]{false,true,true,true,true,true,true,true,true,true,true,true,true,true});
        assertFalse( t.isSelected(), "bool selected");
        assertEquals( TriStateJCheckBox.State.PARTIAL, t.getState(), "bool partial");

    }

    @Test
    public void testSetSelected() {

        TriStateJCheckBox t = new TriStateJCheckBox("");

        t.setSelected(true);
        assertTrue( t.isSelected(), "selected");
        assertEquals( TriStateJCheckBox.State.CHECKED, t.getState(), "checked");

        t.setSelected(false);
        assertFalse( t.isSelected(), "not selected");
        assertEquals( TriStateJCheckBox.State.UNCHECKED, t.getState(), "unchecked");

    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testClickable() {

        TriStateJCheckBox t = new TriStateJCheckBox("TriState");

        JmriJFrame f = new JmriJFrame();
        f.add(t);
        f.setTitle(t.getName()); // "TriState"

        jmri.util.ThreadingUtil.runOnGUI( () -> {
            f.pack();
            f.setVisible(true);
        });

        // Find new window by name
        JFrameOperator jfo = new JFrameOperator( t.getName() );

        // Find hardware address field
        JLabelOperator jlo = new JLabelOperator(jfo,t.getName());


        JCheckBox jcb = (JCheckBox) jlo.getLabelFor();
        assertNotNull( jcb, "tsjcb");
        JCheckBoxOperator jcbo = new JCheckBoxOperator(jcb);

        assertTrue( jcbo.isShowing(), "visible");
        assertFalse( jcbo.isSelected(), "Not Selected");

        jcbo.doClick();
        assertTrue( jcbo.isSelected(), "Selected");

        jcbo.doClick();
        assertFalse( jcbo.isSelected(), "Back to not Selected");

        t.setState( new boolean[]{true, true});
        assertTrue( jcbo.isSelected(), "Selected from setState");

        t.setState( new boolean[]{true, false});
        assertFalse( jcbo.isSelected(), "Partial Not Selected from setState");

        jcbo.doClick();
        assertFalse( jcbo.isSelected(), "Still not Selected following click from partial");

        t.setState( new boolean[]{true, true});
        assertTrue( jcbo.isSelected(), "Selected from setState");

        t.setState( new boolean[]{false, false});
        assertFalse( jcbo.isSelected(), "Not Selected from setState");


        jlo.clickMouse();
        assertTrue( jcbo.isSelected(), "Selected from click Label");

        jlo.clickMouse();
        assertFalse( jcbo.isSelected(), "Not Selected from click Label");

        jlo.enterMouse();
        assertTrue(jcbo.isEnabled());

        jlo.exitMouse();
        assertTrue(jcbo.isEnabled());

        t.setEnabled(false);
        jcbo.doClick();
        assertFalse( jcbo.isSelected(), "Still not Selected following click as not Enabled");


        t.setState( new boolean[]{true, true});
        assertTrue( jcbo.isSelected(), "disabled Selected from setState ");

        t.setState( new boolean[]{true, false});
        assertFalse( jcbo.isSelected(), "disabled Partial Not Selected from setState");

        t.setSelected( true);
        assertTrue( jcbo.isSelected(), "disabled Selected from setSelected ");

        jlo.clickMouse();
        assertTrue( jcbo.isSelected(), "still Selected from setSelected ");

        // Ask to close window
        JUnitUtil.dispose(jfo.getWindow());
        jfo.waitClosed();

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
