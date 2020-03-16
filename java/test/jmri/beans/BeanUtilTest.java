package jmri.beans;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.util.JUnitUtil;

/**
 * Tests for {@link jmri.beans.BeanUtil} static methods.
 *
 * @author Randall Wood
 */
public class BeanUtilTest {

    private static final String CLASS = "class";
    private static final String PROPERTY_NAMES = "propertyNames";
    private static final String STRING_PROPERTY = "stringProperty";
    private static final String INDEXED_PROPERTY = "indexedProperty";
    private static final String NOT_A_PROPERTY = "nonExistentProperty";
    private static final String OLD_VALUE = "old";
    private static final String NEW_VALUE = "new";

    private Object interfaceTarget;
    private Object hashedTarget;
    private Object introspectedTarget;

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        interfaceTarget = new InterfaceTarget();
        hashedTarget = new ArbitraryTarget();
        introspectedTarget = new Target();
    }

    @AfterEach
    public void tearDown() throws Exception {
        interfaceTarget = null;
        hashedTarget = null;
        introspectedTarget = null;
        JUnitUtil.tearDown();
    }

    /**
     * Test of setIndexedProperty method, of class BeanUtil.
     */
    @Test
    public void testSetIndexedProperty() {
        BeanUtil.setIndexedProperty(interfaceTarget, INDEXED_PROPERTY, 1, NEW_VALUE);
        BeanUtil.setIndexedProperty(introspectedTarget, INDEXED_PROPERTY, 1, NEW_VALUE);
        BeanUtil.setIndexedProperty(hashedTarget, INDEXED_PROPERTY, 1, NEW_VALUE);
        assertThat(BeanUtil.getIndexedProperty(interfaceTarget, INDEXED_PROPERTY, 0)).isEqualTo(OLD_VALUE);
        assertThat(BeanUtil.getIndexedProperty(interfaceTarget, INDEXED_PROPERTY, 1)).isEqualTo(NEW_VALUE);
        assertThat(BeanUtil.getIndexedProperty(introspectedTarget, INDEXED_PROPERTY, 0)).isEqualTo(OLD_VALUE);
        assertThat(BeanUtil.getIndexedProperty(introspectedTarget, INDEXED_PROPERTY, 1)).isEqualTo(NEW_VALUE);
        assertThat(BeanUtil.getIndexedProperty(hashedTarget, INDEXED_PROPERTY, 0)).isEqualTo(OLD_VALUE);
        assertThat(BeanUtil.getIndexedProperty(hashedTarget, INDEXED_PROPERTY, 1)).isEqualTo(NEW_VALUE);
    }

    /**
     * Test of setIntrospectedIndexedProperty method, of class BeanUtil.
     */
    @Test
    public void testSetIntrospectedIndexedProperty() {
        BeanUtil.setIntrospectedIndexedProperty(interfaceTarget, INDEXED_PROPERTY, 1, NEW_VALUE);
        BeanUtil.setIntrospectedIndexedProperty(introspectedTarget, INDEXED_PROPERTY, 1, NEW_VALUE);
        BeanUtil.setIntrospectedIndexedProperty(hashedTarget, INDEXED_PROPERTY, 1, NEW_VALUE);
        assertThat(BeanUtil.getIntrospectedIndexedProperty(interfaceTarget, INDEXED_PROPERTY, 0)).isEqualTo(OLD_VALUE);
        assertThat(BeanUtil.getIntrospectedIndexedProperty(interfaceTarget, INDEXED_PROPERTY, 1)).isEqualTo(NEW_VALUE);
        assertThat(BeanUtil.getIntrospectedIndexedProperty(introspectedTarget, INDEXED_PROPERTY, 0)).isEqualTo(OLD_VALUE);
        assertThat(BeanUtil.getIntrospectedIndexedProperty(introspectedTarget, INDEXED_PROPERTY, 1)).isEqualTo(NEW_VALUE);
        assertThat(BeanUtil.getIntrospectedIndexedProperty(hashedTarget, INDEXED_PROPERTY, 0)).isNull();
        assertThat(BeanUtil.getIntrospectedIndexedProperty(hashedTarget, INDEXED_PROPERTY, 1)).isNull();
        // either parameter being null should silently succeed
        assertThatCode(() -> BeanUtil.setIntrospectedIndexedProperty(null, INDEXED_PROPERTY, 0, NEW_VALUE)).doesNotThrowAnyException();
        assertThatCode(() -> BeanUtil.setIntrospectedIndexedProperty(new Object(), null, 0, NEW_VALUE)).doesNotThrowAnyException();
    }

    /**
     * Test of getIndexedProperty method, of class BeanUtil.
     */
    @Test
    public void testGetIndexedProperty() {
        assertThat(BeanUtil.getIndexedProperty(interfaceTarget, INDEXED_PROPERTY, 0)).isEqualTo(OLD_VALUE);
        assertThat(BeanUtil.getIndexedProperty(interfaceTarget, STRING_PROPERTY, 0)).isNull();
        assertThat(BeanUtil.getIndexedProperty(interfaceTarget, NOT_A_PROPERTY, 0)).isNull();
        assertThat(BeanUtil.getIndexedProperty(introspectedTarget, INDEXED_PROPERTY, 0)).isEqualTo(OLD_VALUE);
        assertThat(BeanUtil.getIndexedProperty(introspectedTarget, STRING_PROPERTY, 0)).isNull();
        assertThat(BeanUtil.getIndexedProperty(introspectedTarget, NOT_A_PROPERTY, 0)).isNull();
        assertThat(BeanUtil.getIndexedProperty(hashedTarget, INDEXED_PROPERTY, 0)).isEqualTo(OLD_VALUE);
        assertThat(BeanUtil.getIndexedProperty(hashedTarget, STRING_PROPERTY, 0)).isNull();
        assertThat(BeanUtil.getIndexedProperty(hashedTarget, NOT_A_PROPERTY, 0)).isNull();
    }

    /**
     * Test of getIntrospectedIndexedProperty method, of class BeanUtil.
     */
    @Test
    public void testGetIntrospectedIndexedProperty() {
        assertThat(BeanUtil.getIntrospectedIndexedProperty(interfaceTarget, INDEXED_PROPERTY, 0)).isEqualTo(OLD_VALUE);
        assertThat(BeanUtil.getIntrospectedIndexedProperty(interfaceTarget, STRING_PROPERTY, 0)).isNull();
        assertThat(BeanUtil.getIntrospectedIndexedProperty(interfaceTarget, NOT_A_PROPERTY, 0)).isNull();
        assertThat(BeanUtil.getIntrospectedIndexedProperty(introspectedTarget, INDEXED_PROPERTY, 0)).isEqualTo(OLD_VALUE);
        assertThat(BeanUtil.getIntrospectedIndexedProperty(introspectedTarget, STRING_PROPERTY, 0)).isNull();
        assertThat(BeanUtil.getIntrospectedIndexedProperty(introspectedTarget, NOT_A_PROPERTY, 0)).isNull();
        assertThat(BeanUtil.getIntrospectedIndexedProperty(hashedTarget, INDEXED_PROPERTY, 0)).isNull();
        assertThat(BeanUtil.getIntrospectedIndexedProperty(hashedTarget, STRING_PROPERTY, 0)).isNull();
        assertThat(BeanUtil.getIntrospectedIndexedProperty(hashedTarget, NOT_A_PROPERTY, 0)).isNull();
        // either parameter being null returns null
        assertThat(BeanUtil.getIntrospectedIndexedProperty(null, INDEXED_PROPERTY, 0)).isNull();
        assertThat(BeanUtil.getIntrospectedIndexedProperty(new Object(), null, 0)).isNull();
    }

    /**
     * Test of setProperty method, of class BeanUtil.
     */
    @Test
    public void testSetProperty() {
        BeanUtil.setProperty(interfaceTarget, STRING_PROPERTY, NEW_VALUE);
        BeanUtil.setProperty(introspectedTarget, STRING_PROPERTY, NEW_VALUE);
        BeanUtil.setProperty(hashedTarget, STRING_PROPERTY, NEW_VALUE);
        assertThat(BeanUtil.getProperty(interfaceTarget, STRING_PROPERTY)).isEqualTo(NEW_VALUE);
        assertThat(BeanUtil.getProperty(introspectedTarget, STRING_PROPERTY)).isEqualTo(NEW_VALUE);
        assertThat(BeanUtil.getProperty(hashedTarget, STRING_PROPERTY)).isEqualTo(NEW_VALUE);
    }

    /**
     * Test of setIntrospectedProperty method, of class BeanUtil.
     */
    @Test
    public void testSetIntrospectedProperty() {
        BeanUtil.setIntrospectedProperty(interfaceTarget, STRING_PROPERTY, NEW_VALUE);
        BeanUtil.setIntrospectedProperty(introspectedTarget, STRING_PROPERTY, NEW_VALUE);
        BeanUtil.setIntrospectedProperty(hashedTarget, STRING_PROPERTY, NEW_VALUE);
        assertThat(BeanUtil.getProperty(interfaceTarget, STRING_PROPERTY)).isEqualTo(NEW_VALUE);
        assertThat(BeanUtil.getProperty(introspectedTarget, STRING_PROPERTY)).isEqualTo(NEW_VALUE);
        assertThat(BeanUtil.getProperty(hashedTarget, STRING_PROPERTY)).isNotEqualTo(NEW_VALUE);
        // either parameter being null should silently succeed
        assertThatCode(() -> BeanUtil.setIntrospectedProperty(null, INDEXED_PROPERTY, NEW_VALUE)).doesNotThrowAnyException();
        assertThatCode(() -> BeanUtil.setIntrospectedProperty(new Object(), null, NEW_VALUE)).doesNotThrowAnyException();
    }

    /**
     * Test of getProperty method, of class BeanUtil.
     */
    @Test
    public void testGetProperty() {
        assertThat(BeanUtil.getProperty(interfaceTarget, STRING_PROPERTY)).isEqualTo(OLD_VALUE);
        assertThat(BeanUtil.getProperty(interfaceTarget, NOT_A_PROPERTY)).isNull();
        assertThat(BeanUtil.getProperty(introspectedTarget, STRING_PROPERTY)).isEqualTo(OLD_VALUE);
        assertThat(BeanUtil.getProperty(introspectedTarget, NOT_A_PROPERTY)).isNull();
        assertThat(BeanUtil.getProperty(hashedTarget, STRING_PROPERTY)).isEqualTo(OLD_VALUE);
        assertThat(BeanUtil.getProperty(hashedTarget, NOT_A_PROPERTY)).isNull();
    }

    /**
     * Test of getIntrospectedProperty method, of class BeanUtil.
     */
    @Test
    public void testGetIntrospectedProperty() {
        assertThat(BeanUtil.getIntrospectedProperty(interfaceTarget, STRING_PROPERTY)).isEqualTo(OLD_VALUE);
        assertThat(BeanUtil.getIntrospectedProperty(interfaceTarget, NOT_A_PROPERTY)).isNull();
        assertThat(BeanUtil.getIntrospectedProperty(introspectedTarget, STRING_PROPERTY)).isEqualTo(OLD_VALUE);
        assertThat(BeanUtil.getIntrospectedProperty(introspectedTarget, NOT_A_PROPERTY)).isNull();
        assertThat(BeanUtil.getIntrospectedProperty(hashedTarget, STRING_PROPERTY)).isNull();
        assertThat(BeanUtil.getIntrospectedProperty(hashedTarget, NOT_A_PROPERTY)).isNull();
        // either parameter being null returns null
        assertThat(BeanUtil.getIntrospectedProperty(null, INDEXED_PROPERTY)).isNull();
        assertThat(BeanUtil.getIntrospectedProperty(new Object(), null)).isNull();
    }

    /**
     * Test of hasProperty method, of class BeanUtil.
     */
    @Test
    public void testHasProperty() {
        assertThat(BeanUtil.hasProperty(new Object(), CLASS)).isTrue();
        // Object should not have this property
        assertThat(BeanUtil.hasProperty(new Object(), STRING_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasProperty(interfaceTarget, STRING_PROPERTY)).isTrue();
        assertThat(BeanUtil.hasProperty(interfaceTarget, NOT_A_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasProperty(introspectedTarget, STRING_PROPERTY)).isTrue();
        assertThat(BeanUtil.hasProperty(introspectedTarget, NOT_A_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasProperty(hashedTarget, STRING_PROPERTY)).isTrue();
        assertThat(BeanUtil.hasProperty(hashedTarget, NOT_A_PROPERTY)).isFalse();
    }

    @Test
    public void testHasIndexedProperty() {
        // class is not indexed
        assertThat(BeanUtil.hasIndexedProperty(new Object(), CLASS)).isFalse();
        // Object should not have this property
        assertThat(BeanUtil.hasIndexedProperty(new Object(), STRING_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIndexedProperty(interfaceTarget, STRING_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIndexedProperty(interfaceTarget, NOT_A_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIndexedProperty(interfaceTarget, INDEXED_PROPERTY)).isTrue();
        assertThat(BeanUtil.hasIndexedProperty(introspectedTarget, STRING_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIndexedProperty(introspectedTarget, NOT_A_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIndexedProperty(introspectedTarget, INDEXED_PROPERTY)).isTrue();
        assertThat(BeanUtil.hasIndexedProperty(hashedTarget, STRING_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIndexedProperty(hashedTarget, NOT_A_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIndexedProperty(hashedTarget, INDEXED_PROPERTY)).isTrue();
    }

    /**
     * Test of hasIntrospectedProperty method, of class BeanUtil.
     */
    @Test
    public void testHasIntrospectedProperty() {
        assertThat(BeanUtil.hasIntrospectedProperty(new Object(), CLASS)).isTrue();
        // Object should not have this property
        assertThat(BeanUtil.hasIntrospectedProperty(new Object(), STRING_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIntrospectedProperty(interfaceTarget, STRING_PROPERTY)).isTrue();
        assertThat(BeanUtil.hasIntrospectedProperty(introspectedTarget, STRING_PROPERTY)).isTrue();
        // "stringProperty" is not discoverable via introspection in
        // hashedTarget
        assertThat(BeanUtil.hasIntrospectedProperty(hashedTarget, STRING_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIntrospectedProperty(interfaceTarget, NOT_A_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIntrospectedProperty(introspectedTarget, NOT_A_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIntrospectedProperty(hashedTarget, NOT_A_PROPERTY)).isFalse();
        // either parameter being null returns false
        assertThat(BeanUtil.hasIntrospectedProperty(null, INDEXED_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIntrospectedProperty(new Object(), null)).isFalse();
    }

    @Test
    public void testHasIntrospectedIndexedProperty() {
        // class is not indexed
        assertThat(BeanUtil.hasIntrospectedIndexedProperty(new Object(), CLASS)).isFalse();
        // Object should not have this property
        assertThat(BeanUtil.hasIntrospectedIndexedProperty(new Object(), STRING_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIntrospectedIndexedProperty(interfaceTarget, STRING_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIntrospectedIndexedProperty(interfaceTarget, NOT_A_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIntrospectedIndexedProperty(interfaceTarget, INDEXED_PROPERTY)).isTrue();
        assertThat(BeanUtil.hasIntrospectedIndexedProperty(introspectedTarget, STRING_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIntrospectedIndexedProperty(introspectedTarget, NOT_A_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIntrospectedIndexedProperty(introspectedTarget, INDEXED_PROPERTY)).isTrue();
        assertThat(BeanUtil.hasIntrospectedIndexedProperty(hashedTarget, STRING_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIntrospectedIndexedProperty(hashedTarget, NOT_A_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIntrospectedIndexedProperty(hashedTarget, INDEXED_PROPERTY)).isFalse();
        // either parameter being null returns false
        assertThat(BeanUtil.hasIntrospectedIndexedProperty(null, INDEXED_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIntrospectedIndexedProperty(new Object(), null)).isFalse();
    }

    /**
     * Test getting properties via getPropertyNames.
     */
    @Test
    public void testGetPropertyNames() {
        assertThat(BeanUtil.getPropertyNames(new Object())).containsExactly(CLASS);
        assertThat(BeanUtil.getPropertyNames(interfaceTarget)).containsExactlyInAnyOrder(CLASS, PROPERTY_NAMES, STRING_PROPERTY, INDEXED_PROPERTY);
        assertThat(BeanUtil.getPropertyNames(introspectedTarget)).containsExactlyInAnyOrder(CLASS, STRING_PROPERTY, INDEXED_PROPERTY);
        assertThat(BeanUtil.getPropertyNames(hashedTarget)).containsExactlyInAnyOrder(CLASS, PROPERTY_NAMES, STRING_PROPERTY, INDEXED_PROPERTY);
        assertThat(BeanUtil.getPropertyNames(null)).isEmpty();
    }

    /**
     * Test getting introspected properties via getIntrospectedPropertyNames.
     */
    @Test
    public void testGetIntrospectedPropertyNames() {
        assertThat(BeanUtil.getIntrospectedPropertyNames(new Object())).containsExactly(CLASS);
        assertThat(BeanUtil.getIntrospectedPropertyNames(interfaceTarget)).containsExactlyInAnyOrder(CLASS, PROPERTY_NAMES, STRING_PROPERTY, INDEXED_PROPERTY);
        assertThat(BeanUtil.getIntrospectedPropertyNames(introspectedTarget)).containsExactlyInAnyOrder(CLASS, STRING_PROPERTY, INDEXED_PROPERTY);
        assertThat(BeanUtil.getIntrospectedPropertyNames(hashedTarget)).containsExactlyInAnyOrder(CLASS, PROPERTY_NAMES);
        assertThat(BeanUtil.getIntrospectedPropertyNames(null)).isEmpty();
    }

    @Test
    public void testImplementsBeanInterface() {
        assertThat(BeanUtil.implementsBeanInterface(interfaceTarget)).isTrue();
        assertThat(BeanUtil.implementsBeanInterface(introspectedTarget)).isFalse();
        assertThat(BeanUtil.implementsBeanInterface(hashedTarget)).isTrue();
        assertThat(BeanUtil.implementsBeanInterface(null)).isFalse();
        assertThat(BeanUtil.implementsBeanInterface(new Object())).isFalse();
        assertThat(BeanUtil.implementsBeanInterface(new Bean() {
        })).isTrue();
    }

    @Test
    public void testContains() {
        Listener l1 = new Listener();
        Listener l2 = new Listener();
        Listener l3 = new Listener();
        PropertyChangeListener[] listeners = {l1, l2};
        assertThat(BeanUtil.contains(listeners, l1)).isTrue();
        assertThat(BeanUtil.contains(listeners, l2)).isTrue();
        assertThat(BeanUtil.contains(listeners, l3)).isFalse();
        listeners[1] = new PropertyChangeListenerProxy("foo", l2);
        assertThat(BeanUtil.contains(listeners, l1)).isTrue();
        assertThat(BeanUtil.contains(listeners, l2)).isTrue();
        assertThat(BeanUtil.contains(listeners, l3)).isFalse();
    }

    /*
     * The following three classes define the properties "stringProperty" and
     * "indexedProperty", however ArbitraryTarget uses a HashMap to define those
     * properties, while InterfaceTarget and Target use standard JavaBeans APIs
     * and conventions.
     */
    public class InterfaceTarget extends UnboundBean {

        private String stringProperty = OLD_VALUE;
        private final ArrayList<String> indexedProperty = new ArrayList<>();

        public InterfaceTarget() {
            this.indexedProperty.add(0, OLD_VALUE);
        }

        public String getStringProperty() {
            return this.stringProperty;
        }

        public void setStringProperty(String stringProperty) {
            this.stringProperty = stringProperty;
        }

        public String getIndexedProperty(int index) {
            return indexedProperty.get(index);
        }

        /*
         * Throws IndexOutOfBoundsException if index > size.
         */
        public void setIndexedProperty(int index, String string) {
            if (index < this.indexedProperty.size()) {
                this.indexedProperty.set(index, string);
            } else {
                this.indexedProperty.add(index, string);
            }
        }
    }

    /*
     * The properties "stringProperty" and "indexedProperty" are not visible in
     * the "*Introspected*" tests, but are exposed using jmri.beans.BeanUtil
     * methods in other tests.
     */
    public class ArbitraryTarget extends UnboundArbitraryBean {

        public ArbitraryTarget() {
            this.setProperty(STRING_PROPERTY, OLD_VALUE);
            this.setIndexedProperty(INDEXED_PROPERTY, 0, OLD_VALUE);
        }
    }

    public class Target {

        private String stringProperty = OLD_VALUE;
        private final ArrayList<String> indexedProperty = new ArrayList<>();

        public Target() {
            this.indexedProperty.add(0, OLD_VALUE);
        }

        public String getStringProperty() {
            return this.stringProperty;
        }

        public void setStringProperty(String stringProperty) {
            this.stringProperty = stringProperty;
        }

        public String getIndexedProperty(int index) {
            return indexedProperty.get(index);
        }

        /*
         * Throws IndexOutOfBoundsException if index > size.
         */
        public void setIndexedProperty(int index, String string) {
            if (index < this.indexedProperty.size()) {
                this.indexedProperty.set(index, string);
            } else {
                this.indexedProperty.add(index, string);
            }
        }
    }

    /*
     * A simple listener class to avoid too many anonymous identical objects.
     */
    private class Listener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            // do nothing
        }

    }
}
