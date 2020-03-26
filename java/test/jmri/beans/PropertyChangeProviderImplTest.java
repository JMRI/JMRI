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
public class PropertyChangeProviderImplTest {

    private PropertyChangeProviderImpl instance;
    private PropertyChangeListener listener;
    private static String PROPERTY = "property";

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

    @BeforeEach
    public void setup() {
        JUnitUtil.setUp();
        instance = new PropertyChangeProviderImpl();
        listener = (PropertyChangeEvent evt) -> {};
    }
    
    @AfterEach
    public void tearDown() {
        instance = null;
        JUnitUtil.tearDown();
    }
}
