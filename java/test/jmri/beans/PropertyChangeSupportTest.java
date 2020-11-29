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
public class PropertyChangeSupportTest {

    private PropertyChangeSupport instance;
    private TestPropertyChangeListener listener;
    private static final String PROPERTY = "property";

    @Test
    public void testAddPropertyChangeListener_PropertyChangeListener() {
        assertThat(instance.getPropertyChangeListeners()).isEmpty();
        assertThat(instance.getPropertyChangeListeners(PROPERTY)).isEmpty();
        instance.addPropertyChangeListener(listener);
        assertThat(instance.getPropertyChangeListeners()).containsExactly(listener);
        assertThat(instance.getPropertyChangeListeners(PROPERTY)).isEmpty();
    }

    @Test
    public void testAddPropertyChangeListener_String_PropertyChangeListener() {
        assertThat(instance.getPropertyChangeListeners()).isEmpty();
        assertThat(instance.getPropertyChangeListeners(PROPERTY)).isEmpty();
        instance.addPropertyChangeListener(PROPERTY, listener);
        PropertyChangeListener listener2 = instance.getPropertyChangeListeners()[0];
        assertThat(listener2).isExactlyInstanceOf(PropertyChangeListenerProxy.class);
        assertThat(((PropertyChangeListenerProxy) listener2).getListener()).isEqualTo(listener);
        assertThat(((PropertyChangeListenerProxy) listener2).getPropertyName()).isEqualTo(PROPERTY);
        assertThat(instance.getPropertyChangeListeners(PROPERTY)).containsExactly(listener);
    }

    @Test
    public void testRemovePropertyChangeListener_PropertyChangeListener() {
        instance.addPropertyChangeListener(listener);
        assertThat(instance.getPropertyChangeListeners()).containsExactly(listener);
        assertThat(instance.getPropertyChangeListeners(PROPERTY)).isEmpty();
        instance.removePropertyChangeListener(listener);
        assertThat(instance.getPropertyChangeListeners()).isEmpty();
        assertThat(instance.getPropertyChangeListeners(PROPERTY)).isEmpty();
    }

    @Test
    public void testRemovePropertyChangeListener_String_PropertyChangeListener() {
        instance.addPropertyChangeListener(PROPERTY, listener);
        assertThat(instance.getPropertyChangeListeners()[0]).isExactlyInstanceOf(PropertyChangeListenerProxy.class);
        assertThat(instance.getPropertyChangeListeners(PROPERTY)).containsExactly(listener);
        instance.removePropertyChangeListener(PROPERTY, listener);
        assertThat(instance.getPropertyChangeListeners()).isEmpty();
        assertThat(instance.getPropertyChangeListeners(PROPERTY)).isEmpty();
    }

    @Test
    public void testFireIndexedPropertyChange_String_int_boolean_boolean() {
        instance.addPropertyChangeListener(listener);
        instance.fireIndexedPropertyChange(PROPERTY, 0, false, true);
        assertThat(listener.getEvents().size()).isEqualTo(1);
        PropertyChangeEvent event = listener.getLastEvent();
        assertThat(event.getPropertyName()).isEqualTo(PROPERTY);
        assertThat(event.getOldValue()).isEqualTo(false);
        assertThat(event.getNewValue()).isEqualTo(true);
        listener.clear();
        assertThat(listener.getEvents()).isEmpty();
        instance.fireIndexedPropertyChange(PROPERTY, 0, false, false);
        assertThat(listener.getEvents()).isEmpty();
    }

    @Test
    public void testFireIndexedPropertyChange_String_int_int_int() {
        instance.addPropertyChangeListener(listener);
        instance.fireIndexedPropertyChange(PROPERTY, 0, -1, 1);
        assertThat(listener.getEvents().size()).isEqualTo(1);
        PropertyChangeEvent event = listener.getLastEvent();
        assertThat(event.getPropertyName()).isEqualTo(PROPERTY);
        assertThat(event.getOldValue()).isEqualTo(-1);
        assertThat(event.getNewValue()).isEqualTo(1);
        listener.clear();
        assertThat(listener.getEvents()).isEmpty();
        instance.fireIndexedPropertyChange(PROPERTY, 0, 0, 0);
        assertThat(listener.getEvents()).isEmpty();
    }

    @Test
    public void testFireIndexedPropertyChange_String_int_Object_Object() {
        instance.addPropertyChangeListener(listener);
        Object object1 = new Object();
        Object object2 = new Object();
        assertThat(object1).isNotEqualTo(object2);
        instance.fireIndexedPropertyChange(PROPERTY, 0, object1, object2);
        assertThat(listener.getEvents().size()).isEqualTo(1);
        PropertyChangeEvent event = listener.getLastEvent();
        assertThat(event.getPropertyName()).isEqualTo(PROPERTY);
        assertThat(event.getOldValue()).isEqualTo(object1);
        assertThat(event.getNewValue()).isEqualTo(object2);
        listener.clear();
        assertThat(listener.getEvents()).isEmpty();
        instance.fireIndexedPropertyChange(PROPERTY, 0, object1, object1);
        assertThat(listener.getEvents()).isEmpty();
        instance.fireIndexedPropertyChange(PROPERTY, 0, object1, null);
        event = listener.getLastEvent();
        assertThat(event.getPropertyName()).isEqualTo(PROPERTY);
        assertThat(event.getOldValue()).isEqualTo(object1);
        assertThat(event.getNewValue()).isEqualTo(null);
        instance.fireIndexedPropertyChange(PROPERTY, 0, null, object2);
        event = listener.getLastEvent();
        assertThat(event.getPropertyName()).isEqualTo(PROPERTY);
        assertThat(event.getOldValue()).isEqualTo(null);
        assertThat(event.getNewValue()).isEqualTo(object2);
        instance.fireIndexedPropertyChange(PROPERTY, 0, null, null);
        event = listener.getLastEvent();
        assertThat(event.getPropertyName()).isEqualTo(PROPERTY);
        assertThat(event.getOldValue()).isEqualTo(null);
        assertThat(event.getNewValue()).isEqualTo(null);
    }

    @Test
    public void testFirePropertyChange_String_int_boolean_boolean() {
        instance.addPropertyChangeListener(listener);
        instance.firePropertyChange(PROPERTY, false, true);
        assertThat(listener.getEvents().size()).isEqualTo(1);
        PropertyChangeEvent event = listener.getLastEvent();
        assertThat(event.getPropertyName()).isEqualTo(PROPERTY);
        assertThat(event.getOldValue()).isEqualTo(false);
        assertThat(event.getNewValue()).isEqualTo(true);
        listener.clear();
        assertThat(listener.getEvents()).isEmpty();
        instance.firePropertyChange(PROPERTY, false, false);
        assertThat(listener.getEvents()).isEmpty();
    }

    @Test
    public void testFirePropertyChange_String_int_int_int() {
        instance.addPropertyChangeListener(listener);
        instance.firePropertyChange(PROPERTY, -1, 1);
        assertThat(listener.getEvents().size()).isEqualTo(1);
        PropertyChangeEvent event = listener.getLastEvent();
        assertThat(event.getPropertyName()).isEqualTo(PROPERTY);
        assertThat(event.getOldValue()).isEqualTo(-1);
        assertThat(event.getNewValue()).isEqualTo(1);
        listener.clear();
        assertThat(listener.getEvents()).isEmpty();
        instance.firePropertyChange(PROPERTY, 0, 0);
        assertThat(listener.getEvents()).isEmpty();
    }

    @Test
    public void testFirePropertyChange_String_int_Object_Object() {
        instance.addPropertyChangeListener(listener);
        Object object1 = new Object();
        Object object2 = new Object();
        assertThat(object1).isNotEqualTo(object2);
        instance.firePropertyChange(PROPERTY, object1, object2);
        assertThat(listener.getEvents().size()).isEqualTo(1);
        PropertyChangeEvent event = listener.getLastEvent();
        assertThat(event.getPropertyName()).isEqualTo(PROPERTY);
        assertThat(event.getOldValue()).isEqualTo(object1);
        assertThat(event.getNewValue()).isEqualTo(object2);
        listener.clear();
        assertThat(listener.getEvents()).isEmpty();
        instance.firePropertyChange(PROPERTY, object1, object1);
        assertThat(listener.getEvents()).isEmpty();
        instance.firePropertyChange(PROPERTY, object1, null);
        event = listener.getLastEvent();
        assertThat(event.getPropertyName()).isEqualTo(PROPERTY);
        assertThat(event.getOldValue()).isEqualTo(object1);
        assertThat(event.getNewValue()).isEqualTo(null);
        instance.firePropertyChange(PROPERTY, null, object2);
        event = listener.getLastEvent();
        assertThat(event.getPropertyName()).isEqualTo(PROPERTY);
        assertThat(event.getOldValue()).isEqualTo(null);
        assertThat(event.getNewValue()).isEqualTo(object2);
        instance.firePropertyChange(PROPERTY, null, null);
        event = listener.getLastEvent();
        assertThat(event.getPropertyName()).isEqualTo(PROPERTY);
        assertThat(event.getOldValue()).isEqualTo(null);
        assertThat(event.getNewValue()).isEqualTo(null);
    }

    @Test
    public void testFirePropertyChange_PropertyChangeEvent() {
        instance.addPropertyChangeListener(listener);
        Object object1 = new Object();
        Object object2 = new Object();
        assertThat(object1).isNotEqualTo(object2);
        PropertyChangeEvent sent = new PropertyChangeEvent(instance, PROPERTY, object1, object2);
        instance.firePropertyChange(sent);
        assertThat(listener.getEvents().size()).isEqualTo(1);
        assertThat(listener.getLastEvent()).isEqualTo(sent);
    }

    @BeforeEach
    public void setup() {
        JUnitUtil.setUp();
        instance = new PropertyChangeSupport();
        listener = new TestPropertyChangeListener();
    }

    @AfterEach
    public void tearDown() {
        instance = null;
        listener = null;
        JUnitUtil.tearDown();
    }
}
