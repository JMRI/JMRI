package jmri.beans;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.SwingUtilities;

import jmri.util.JUnitUtil;

import org.assertj.swing.edt.GuiActionRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Randall Wood Copyright 2020
 */
public class SwingPropertyChangeListenerTest {

    private SwingPropertyChangeListener s;
    private PropertyChangeListener l;
    private PropertyChangeEvent e;
    private boolean notifiedOnEDT;

    @Test
    public void testPropertyChangeOffEDT() {
        s = new SwingPropertyChangeListener(l, false);
        assertThat(notifiedOnEDT).isFalse();
        s.propertyChange(e);
        assertThat(notifiedOnEDT).isFalse();
    }

    @Test
    public void testPropertyChangeOnEDT() {
        s = new SwingPropertyChangeListener(l, true);
        assertThat(notifiedOnEDT).isFalse();
        GuiActionRunner.execute(() -> s.propertyChange(e));
        assertThat(notifiedOnEDT).isTrue();
    }

    /**
     * Test that {@link Bean#isNotifyOnEDT()}, which provides access to a final
     * value is returning the expected final value for all construction scenarios.
     */
    @Test
    public void testIsNotifyOnEDT() {
        assertThat(new SwingPropertyChangeListener(l).isNotifyOnEDT()).isTrue();
        assertThat(new SwingPropertyChangeListener(l, true).isNotifyOnEDT()).isTrue();
        assertThat(new SwingPropertyChangeListener(l, false).isNotifyOnEDT()).isFalse();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        notifiedOnEDT = false;
        e = new PropertyChangeEvent(this, "test", null, null);
        l = evt -> notifiedOnEDT = SwingUtilities.isEventDispatchThread();
    }
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    
}
