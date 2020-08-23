package jmri.beans;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeListenerProxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.util.JUnitUtil;

/**
 * @author Randall Wood Copyright 2020
 */
public class VetoableChangeSupportTest {

    private VetoableChangeSupport instance;
    private TestVetoableChangeListener listener;
    private static String PROPERTY = "property";
    private static String VETO = "veto";

    @Test
    public void testFireVetoableChange_String_int_boolean_boolean() {
        instance.addVetoableChangeListener(listener);
        assertThat(listener.willThrowNext()).isFalse();
        assertThatCode(() -> instance.fireVetoableChange(PROPERTY, false, true)).doesNotThrowAnyException();
        assertThat(listener.getEvents().size()).isEqualTo(1);
        PropertyChangeEvent event = listener.getLastEvent();
        assertThat(event.getPropertyName()).isEqualTo(PROPERTY);
        assertThat(event.getOldValue()).isEqualTo(false);
        assertThat(event.getNewValue()).isEqualTo(true);
        listener.clear();
        assertThat(listener.getEvents()).isEmpty();
        assertThatCode(() -> instance.fireVetoableChange(PROPERTY, false, false)).doesNotThrowAnyException();
        assertThat(listener.getEvents()).isEmpty();
        listener.throwNext(VETO);
        assertThatCode(() -> instance.fireVetoableChange(PROPERTY, false, true)).isInstanceOf(PropertyVetoException.class).hasMessage(VETO);
    }

    @Test
    public void testFireVetoableChange_String_int_int_int() {
        instance.addVetoableChangeListener(listener);
        assertThat(listener.willThrowNext()).isFalse();
        assertThatCode(() -> instance.fireVetoableChange(PROPERTY, -1, 1)).doesNotThrowAnyException();
        assertThat(listener.getEvents().size()).isEqualTo(1);
        PropertyChangeEvent event = listener.getLastEvent();
        assertThat(event.getPropertyName()).isEqualTo(PROPERTY);
        assertThat(event.getOldValue()).isEqualTo(-1);
        assertThat(event.getNewValue()).isEqualTo(1);
        listener.clear();
        assertThat(listener.getEvents()).isEmpty();
        assertThatCode(() -> instance.fireVetoableChange(PROPERTY, 0, 0)).doesNotThrowAnyException();
        assertThat(listener.getEvents()).isEmpty();
        listener.throwNext(VETO);
        assertThatCode(() -> instance.fireVetoableChange(PROPERTY, -1, 1)).isInstanceOf(PropertyVetoException.class).hasMessage(VETO);
    }

    @Test
    public void testFireVetoableChange_String_int_Object_Object() {
        instance.addVetoableChangeListener(listener);
        Object object1 = new Object();
        Object object2 = new Object();
        assertThat(object1).isNotEqualTo(object2);
        assertThat(listener.willThrowNext()).isFalse();
        assertThatCode(() -> instance.fireVetoableChange(PROPERTY, object1, object2)).doesNotThrowAnyException();
        assertThat(listener.getEvents().size()).isEqualTo(1);
        PropertyChangeEvent event = listener.getLastEvent();
        assertThat(event.getPropertyName()).isEqualTo(PROPERTY);
        assertThat(event.getOldValue()).isEqualTo(object1);
        assertThat(event.getNewValue()).isEqualTo(object2);
        listener.clear();
        assertThat(listener.getEvents()).isEmpty();
        assertThatCode(() -> instance.fireVetoableChange(PROPERTY, object1, object1)).doesNotThrowAnyException();
        assertThat(listener.getEvents()).isEmpty();
        assertThatCode(() -> instance.fireVetoableChange(PROPERTY, object1, null)).doesNotThrowAnyException();
        event = listener.getLastEvent();
        assertThat(event.getPropertyName()).isEqualTo(PROPERTY);
        assertThat(event.getOldValue()).isEqualTo(object1);
        assertThat(event.getNewValue()).isEqualTo(null);
        assertThatCode(() -> instance.fireVetoableChange(PROPERTY, null, object2)).doesNotThrowAnyException();
        event = listener.getLastEvent();
        assertThat(event.getPropertyName()).isEqualTo(PROPERTY);
        assertThat(event.getOldValue()).isEqualTo(null);
        assertThat(event.getNewValue()).isEqualTo(object2);
        assertThatCode(() -> instance.fireVetoableChange(PROPERTY, null, null)).doesNotThrowAnyException();
        event = listener.getLastEvent();
        assertThat(event.getPropertyName()).isEqualTo(PROPERTY);
        assertThat(event.getOldValue()).isEqualTo(null);
        assertThat(event.getNewValue()).isEqualTo(null);
        listener.throwNext(VETO);
        assertThatCode(() -> instance.fireVetoableChange(PROPERTY, object1, object2)).isInstanceOf(PropertyVetoException.class).hasMessage(VETO);
    }

    @Test
    public void testFireVetoableChange_VetoableChangeEvent() {
        instance.addVetoableChangeListener(listener);
        Object object1 = new Object();
        Object object2 = new Object();
        assertThat(object1).isNotEqualTo(object2);
        PropertyChangeEvent sent = new PropertyChangeEvent(instance, PROPERTY, object1, object2);
        assertThatCode(() -> instance.fireVetoableChange(sent)).doesNotThrowAnyException();
        assertThat(listener.getEvents().size()).isEqualTo(1);
        assertThat(listener.getLastEvent()).isEqualTo(sent);
        listener.throwNext(VETO);
        assertThatCode(() -> instance.fireVetoableChange(sent)).isInstanceOf(PropertyVetoException.class).hasMessage(VETO);
    }

    @Test
    public void testAddVetoableChangeListener_VetoableChangeListener() {
        assertThat(instance.getVetoableChangeListeners()).isEmpty();
        assertThat(instance.getVetoableChangeListeners(PROPERTY)).isEmpty();
        instance.addVetoableChangeListener(listener);
        assertThat(instance.getVetoableChangeListeners()).containsExactly(listener);
        assertThat(instance.getVetoableChangeListeners(PROPERTY)).isEmpty();
    }

    @Test
    public void testAddVetoableChangeListener_String_VetoableChangeListener() {
        assertThat(instance.getVetoableChangeListeners()).isEmpty();
        assertThat(instance.getVetoableChangeListeners(PROPERTY)).isEmpty();
        instance.addVetoableChangeListener(PROPERTY, listener);
        VetoableChangeListener listener2 = instance.getVetoableChangeListeners()[0];
        assertThat(listener2).isExactlyInstanceOf(VetoableChangeListenerProxy.class);
        assertThat(((VetoableChangeListenerProxy) listener2).getListener()).isEqualTo(listener);
        assertThat(((VetoableChangeListenerProxy) listener2).getPropertyName()).isEqualTo(PROPERTY);
        assertThat(instance.getVetoableChangeListeners(PROPERTY)).containsExactly(listener);
    }

    @Test
    public void testRemoveVetoableChangeListener_VetoableChangeListener() {
        instance.addVetoableChangeListener(listener);
        assertThat(instance.getVetoableChangeListeners()).containsExactly(listener);
        assertThat(instance.getVetoableChangeListeners(PROPERTY)).isEmpty();
        instance.removeVetoableChangeListener(listener);
        assertThat(instance.getVetoableChangeListeners()).isEmpty();
        assertThat(instance.getVetoableChangeListeners(PROPERTY)).isEmpty();
    }

    @Test
    public void testRemoveVetoableChangeListener_String_VetoableChangeListener() {
        instance.addVetoableChangeListener(PROPERTY, listener);
        assertThat(instance.getVetoableChangeListeners()[0]).isExactlyInstanceOf(VetoableChangeListenerProxy.class);
        assertThat(instance.getVetoableChangeListeners(PROPERTY)).containsExactly(listener);
        instance.removeVetoableChangeListener(PROPERTY, listener);
        assertThat(instance.getVetoableChangeListeners()).isEmpty();
        assertThat(instance.getVetoableChangeListeners(PROPERTY)).isEmpty();
    }

    @BeforeEach
    public void setup() {
        JUnitUtil.setUp();
        instance = new VetoableChangeSupport();
        listener = new TestVetoableChangeListener();
    }

    @AfterEach
    public void tearDown() {
        instance = null;
        listener = null;
        JUnitUtil.tearDown();
    }
}
