package jmri.beans;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.util.JUnitUtil;

/**
 * @author Randall Wood Copyright 2020
 */
public class BeanTest {

    private boolean changed;
    private Bean bean;
    private PropertyChangeListener listener;
    private final static String PROPERTY = "property";

    @Test
    public void testAddPropertyChangeListener_PropertyChangeListener() {
        assertThat(bean.getPropertyChangeListeners()).isEmpty();
        assertThat(bean.getPropertyChangeListeners(PROPERTY)).isEmpty();
        bean.addPropertyChangeListener(listener);
        assertThat(bean.getPropertyChangeListeners()).containsExactly(listener);
        assertThat(bean.getPropertyChangeListeners(PROPERTY)).isEmpty();
    }

    @Test
    public void testAddPropertyChangeListener_String_PropertyChangeListener() {
        assertThat(bean.getPropertyChangeListeners()).isEmpty();
        assertThat(bean.getPropertyChangeListeners(PROPERTY)).isEmpty();
        bean.addPropertyChangeListener(PROPERTY, listener);
        PropertyChangeListener listener2 = bean.getPropertyChangeListeners()[0];
        assertThat(listener2).isExactlyInstanceOf(PropertyChangeListenerProxy.class);
        assertThat(((PropertyChangeListenerProxy) listener2).getListener()).isEqualTo(listener);
        assertThat(((PropertyChangeListenerProxy) listener2).getPropertyName()).isEqualTo(PROPERTY);
        assertThat(bean.getPropertyChangeListeners(PROPERTY)).containsExactly(listener);
    }

    @Test
    public void testRemovePropertyChangeListener_PropertyChangeListener() {
        bean.addPropertyChangeListener(listener);
        assertThat(bean.getPropertyChangeListeners()).containsExactly(listener);
        assertThat(bean.getPropertyChangeListeners(PROPERTY)).isEmpty();
        bean.removePropertyChangeListener(listener);
        assertThat(bean.getPropertyChangeListeners()).isEmpty();
        assertThat(bean.getPropertyChangeListeners(PROPERTY)).isEmpty();
    }

    @Test
    public void testRemovePropertyChangeListener_String_PropertyChangeListener() {
        bean.addPropertyChangeListener(PROPERTY, listener);
        assertThat(bean.getPropertyChangeListeners()[0]).isExactlyInstanceOf(PropertyChangeListenerProxy.class);
        assertThat(bean.getPropertyChangeListeners(PROPERTY)).containsExactly(listener);
        bean.removePropertyChangeListener(PROPERTY, listener);
        assertThat(bean.getPropertyChangeListeners()).isEmpty();
        assertThat(bean.getPropertyChangeListeners(PROPERTY)).isEmpty();
    }

    @Test
    public void testFireIndexedPropertyChange_String_int_boolean_boolean() {
        bean.addPropertyChangeListener(listener);
        assertThat(changed).isFalse();
        bean.fireIndexedPropertyChange(PROPERTY, 0, false, true);
        JUnitUtil.waitFor(() -> {
            return changed == true;
        }, "Change did not fire in time");
        assertThat(changed).isTrue();
    }

    @Test
    public void testFireIndexedPropertyChange_String_int_int_int() {
        bean.addPropertyChangeListener(listener);
        assertThat(changed).isFalse();
        bean.fireIndexedPropertyChange(PROPERTY, 0, 0, 1);
        JUnitUtil.waitFor(() -> {
            return changed == true;
        }, "Change did not fire in time");
        assertThat(changed).isTrue();
    }

    @Test
    public void testFireIndexedPropertyChange_String_int_Object_Object() {
        bean.addPropertyChangeListener(listener);
        assertThat(changed).isFalse();
        bean.fireIndexedPropertyChange(PROPERTY, 0, null, null);
        JUnitUtil.waitFor(() -> {
            return changed == true;
        }, "Change did not fire in time");
        assertThat(changed).isTrue();
    }

    @Test
    public void testFirePropertyChange_PropertyChangeEvent() {
        bean.addPropertyChangeListener(listener);
        PropertyChangeEvent event = new PropertyChangeEvent(bean, PROPERTY, null, null);
        assertThat(changed).isFalse();
        bean.firePropertyChange(event);
        JUnitUtil.waitFor(() -> {
            return changed == true;
        }, "Change did not fire in time");
        assertThat(changed).isTrue();
    }

    @Test
    public void testFirePropertyChange_String_boolean_boolean() {
        bean.addPropertyChangeListener(listener);
        assertThat(changed).isFalse();
        bean.firePropertyChange(PROPERTY, false, true);
        JUnitUtil.waitFor(() -> {
            return changed == true;
        }, "Change did not fire in time");
        assertThat(changed).isTrue();
    }

    @Test
    public void testFirePropertyChange_String_int_int() {
        bean.addPropertyChangeListener(listener);
        assertThat(changed).isFalse();
        bean.firePropertyChange(PROPERTY, 0, 1);
        JUnitUtil.waitFor(() -> {
            return changed == true;
        }, "Change did not fire in time");
        assertThat(changed).isTrue();
    }

    @Test
    public void testFirePropertyChange_String_Object_Object() {
        bean.addPropertyChangeListener(listener);
        assertThat(changed).isFalse();
        bean.firePropertyChange(PROPERTY, null, null);
        JUnitUtil.waitFor(() -> {
            return changed == true;
        }, "Change did not fire in time");
        assertThat(changed).isTrue();
    }

    /**
     * Test that {@link Bean#isNotifyOnEDT()}, which provides access to a final
     * value is returning the expected final value for all construction scenarios.
     */
    @Test
    public void testIsNotifyOnEDT() {
        assertThat(new Bean() {}.isNotifyOnEDT()).isFalse();
        assertThat(new Bean(true) {}.isNotifyOnEDT()).isTrue();
        assertThat(new Bean(false) {}.isNotifyOnEDT()).isFalse();
    }

    @BeforeEach
    public void setup() {
        JUnitUtil.setUp();
        changed = false;
        bean = new Bean() {
        };
        listener = (PropertyChangeEvent evt) -> {
            changed = true;
        };
    }

    @AfterEach
    public void tearDown() {
        bean = null;
        JUnitUtil.tearDown();
    }

}
