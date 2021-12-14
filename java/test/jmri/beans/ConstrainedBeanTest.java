package jmri.beans;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeListenerProxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.util.JUnitUtil;

/**
 * @author Randall Wood Copyright 2020
 */
public class ConstrainedBeanTest {

    private ConstrainedBeanImpl bean;
    private VetoableChangeListener vetoer;
    private boolean changed;
    private boolean veto;
    private static final String PROPERTY = "property";
    private static final String INDEXED = "indexed";

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        bean = new ConstrainedBeanImpl();
        changed = false;
        veto = false;
        vetoer = (e -> {
            if (!veto) {
                changed = true;
            } else {
                veto = false;
                throw new PropertyVetoException("veto", e);
            }
        });
    }

    @AfterEach
    public void tearDown() {
        vetoer = null;
        bean = null;
        JUnitUtil.tearDown();
    }

    @Test
    public void testSetProperty() {
        bean.addVetoableChangeListener(vetoer);
        assertThat(changed).isFalse();
        assertThat(bean.getProperty(PROPERTY)).isEqualTo(null);
        veto = false;
        assertThatCode(() -> bean.setProperty(PROPERTY, PROPERTY)).doesNotThrowAnyException();
        assertThat(bean.getProperty(PROPERTY)).isEqualTo(PROPERTY);
        assertThat(changed).isTrue();
        assertThat(veto).isFalse();
        changed = false;
        veto = true;
        assertThatCode(() -> bean.setProperty(PROPERTY, null)).doesNotThrowAnyException();
        assertThat(bean.getProperty(PROPERTY)).isEqualTo(PROPERTY);
        assertThat(changed).isFalse();
        assertThat(veto).isFalse();
    }

    @Test
    public void testSetIndexedProperty() {
        bean.addVetoableChangeListener(vetoer);
        assertThat(changed).isFalse();
        assertThat(bean.getIndexed()).isEmpty();
        veto = false;
        assertThatCode(() -> bean.setIndexedProperty(INDEXED, 0, INDEXED)).doesNotThrowAnyException();
        assertThat(bean.getIndexedProperty(INDEXED, 0)).isEqualTo(INDEXED);
        assertThat(changed).isTrue();
        assertThat(veto).isFalse();
        changed = false;
        veto = true;
        assertThatCode(() -> bean.setIndexedProperty(INDEXED, 0, null)).doesNotThrowAnyException();
        assertThat(bean.getIndexedProperty(INDEXED, 0)).isEqualTo(INDEXED);
        assertThat(changed).isFalse();
        assertThat(veto).isFalse();
    }

    @Test
    public void testAddVetoableChangeListener_VetoableChangeListener() {
        assertThat(bean.getVetoableChangeListeners()).isEmpty();
        assertThat(bean.getVetoableChangeListeners(PROPERTY)).isEmpty();
        bean.addVetoableChangeListener(vetoer);
        assertThat(bean.getVetoableChangeListeners()).containsExactly(vetoer);
        assertThat(bean.getVetoableChangeListeners(PROPERTY)).isEmpty();
    }

    @Test
    public void testAddVetoableChangeListener_String_VetoableChangeListener() {
        assertThat(bean.getVetoableChangeListeners()).isEmpty();
        assertThat(bean.getVetoableChangeListeners(PROPERTY)).isEmpty();
        bean.addVetoableChangeListener(PROPERTY, vetoer);
        VetoableChangeListener vetoer2 = bean.getVetoableChangeListeners()[0];
        assertThat(vetoer2).isExactlyInstanceOf(VetoableChangeListenerProxy.class);
        assertThat(((VetoableChangeListenerProxy) vetoer2).getListener()).isEqualTo(vetoer);
        assertThat(((VetoableChangeListenerProxy) vetoer2).getPropertyName()).isEqualTo(PROPERTY);
        assertThat(bean.getVetoableChangeListeners(PROPERTY)).containsExactly(vetoer);
    }

    @Test
    public void testRemoveVetoableChangeListener_VetoableChangeListener() {
        bean.addVetoableChangeListener(vetoer);
        assertThat(bean.getVetoableChangeListeners()).containsExactly(vetoer);
        assertThat(bean.getVetoableChangeListeners(PROPERTY)).isEmpty();
        bean.removeVetoableChangeListener(vetoer);
        assertThat(bean.getVetoableChangeListeners()).isEmpty();
        assertThat(bean.getVetoableChangeListeners(PROPERTY)).isEmpty();
    }

    @Test
    public void testRemoveVetoableChangeListener_String_VetoableChangeListener() {
        bean.addVetoableChangeListener(PROPERTY, vetoer);
        assertThat(bean.getVetoableChangeListeners()[0]).isExactlyInstanceOf(VetoableChangeListenerProxy.class);
        assertThat(bean.getVetoableChangeListeners(PROPERTY)).containsExactly(vetoer);
        bean.removeVetoableChangeListener(PROPERTY, vetoer);
        assertThat(bean.getVetoableChangeListeners()).isEmpty();
        assertThat(bean.getVetoableChangeListeners(PROPERTY)).isEmpty();
    }

    @Test
    public void testFireVetoableChange_PropertyChangeEvent() {
        bean.addVetoableChangeListener(vetoer);
        PropertyChangeEvent event = new PropertyChangeEvent(bean, PROPERTY, null, null);
        assertThat(changed).isFalse();
        veto = false;
        assertThatCode(() -> bean.fireVetoableChange(event)).doesNotThrowAnyException();
        assertThat(changed).isTrue();
        assertThat(veto).isFalse();
        changed = false;
        veto = true;
        assertThatThrownBy(() -> bean.fireVetoableChange(event)).isExactlyInstanceOf(PropertyVetoException.class);
        assertThat(changed).isFalse();
        assertThat(veto).isFalse();
    }

    @Test
    public void testFireVetoableChange_String_boolean_boolean() {
        bean.addVetoableChangeListener(vetoer);
        assertThat(changed).isFalse();
        veto = false;
        assertThatCode(() -> bean.fireVetoableChange(PROPERTY, false, true)).doesNotThrowAnyException();
        assertThat(changed).isTrue();
        assertThat(veto).isFalse();
        changed = false;
        veto = true;
        assertThatThrownBy(() -> bean.fireVetoableChange(PROPERTY, false, true))
                .isExactlyInstanceOf(PropertyVetoException.class);
        assertThat(changed).isFalse();
        assertThat(veto).isFalse();
    }

    @Test
    public void testFireVetoableChange_String_int_int() {
        bean.addVetoableChangeListener(vetoer);
        assertThat(changed).isFalse();
        veto = false;
        assertThatCode(() -> bean.fireVetoableChange(PROPERTY, 0, 1)).doesNotThrowAnyException();
        assertThat(changed).isTrue();
        assertThat(veto).isFalse();
        changed = false;
        veto = true;
        assertThatThrownBy(() -> bean.fireVetoableChange(PROPERTY, 0, 1))
                .isExactlyInstanceOf(PropertyVetoException.class);
        assertThat(changed).isFalse();
        assertThat(veto).isFalse();
    }

    @Test
    public void testFireVetoableChange_String_Object_Object() {
        bean.addVetoableChangeListener(vetoer);
        assertThat(changed).isFalse();
        veto = false;
        assertThatCode(() -> bean.fireVetoableChange(PROPERTY, null, null)).doesNotThrowAnyException();
        assertThat(changed).isTrue();
        assertThat(veto).isFalse();
        changed = false;
        veto = true;
        assertThatThrownBy(() -> bean.fireVetoableChange(PROPERTY, null, null))
                .isExactlyInstanceOf(PropertyVetoException.class);
        assertThat(changed).isFalse();
        assertThat(veto).isFalse();
    }

    // for test purposes
    public static class ConstrainedBeanImpl extends ConstrainedBean {

        public ConstrainedBeanImpl() {
        }
        String s;
        List<String> l = new ArrayList<>();

         public String getProperty() {
            return s;
        }

        public void setProperty(String property) {
            s = property;
        }

        public String[] getIndexed() {
            return l.toArray(new String[l.size()]);
        }

        public String getIndexed(int index) {
            return l.get(index);
        }

        public void setIndexed(String[] indexed) {
            l.clear();
            l.addAll(Arrays.asList(indexed));
        }

        public void setIndexed(int index, String indexed) {
            if (index < l.size()) {
                l.set(index, indexed);
            } else {
                l.add(index, indexed);
            }
        }
    }

}
