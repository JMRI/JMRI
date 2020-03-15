package jmri.beans;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

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

    /**
     * Test of setIndexedProperty method, of class BeanUtil.
     */
    @Test
    public void testSetIndexedProperty() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        BeanUtil.setIndexedProperty(introspectedTarget, INDEXED_PROPERTY, 1, NEW_VALUE);
        BeanUtil.setIndexedProperty(hashedTarget, INDEXED_PROPERTY, 1, NEW_VALUE);
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
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        BeanUtil.setIntrospectedIndexedProperty(introspectedTarget, INDEXED_PROPERTY, 1, NEW_VALUE);
        BeanUtil.setIntrospectedIndexedProperty(hashedTarget, INDEXED_PROPERTY, 1, NEW_VALUE);
        assertThat(BeanUtil.getIntrospectedIndexedProperty(introspectedTarget, INDEXED_PROPERTY, 0)).isEqualTo(OLD_VALUE);
        assertThat(BeanUtil.getIntrospectedIndexedProperty(introspectedTarget, INDEXED_PROPERTY, 1)).isEqualTo(NEW_VALUE);
        assertThat(BeanUtil.getIntrospectedIndexedProperty(hashedTarget, INDEXED_PROPERTY, 0)).isNull();
        assertThat(BeanUtil.getIntrospectedIndexedProperty(hashedTarget, INDEXED_PROPERTY, 1)).isNull();
    }

    /**
     * Test of getIndexedProperty method, of class BeanUtil.
     */
    @Test
    public void testGetIndexedProperty() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
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
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        assertThat(BeanUtil.getIntrospectedIndexedProperty(introspectedTarget, INDEXED_PROPERTY, 0)).isEqualTo(OLD_VALUE);
        assertThat(BeanUtil.getIntrospectedIndexedProperty(introspectedTarget, STRING_PROPERTY, 0)).isNull();
        assertThat(BeanUtil.getIntrospectedIndexedProperty(introspectedTarget, NOT_A_PROPERTY, 0)).isNull();
        assertThat(BeanUtil.getIntrospectedIndexedProperty(hashedTarget, INDEXED_PROPERTY, 0)).isNull();
        assertThat(BeanUtil.getIntrospectedIndexedProperty(hashedTarget, STRING_PROPERTY, 0)).isNull();
        assertThat(BeanUtil.getIntrospectedIndexedProperty(hashedTarget, NOT_A_PROPERTY, 0)).isNull();
    }

    /**
     * Test of setProperty method, of class BeanUtil.
     */
    @Test
    public void testSetProperty() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        BeanUtil.setProperty(introspectedTarget, STRING_PROPERTY, NEW_VALUE);
        BeanUtil.setProperty(hashedTarget, STRING_PROPERTY, NEW_VALUE);
        assertThat(BeanUtil.getProperty(introspectedTarget, STRING_PROPERTY)).isEqualTo(NEW_VALUE);
        assertThat(BeanUtil.getProperty(hashedTarget, STRING_PROPERTY)).isEqualTo(NEW_VALUE);
    }

    /**
     * Test of setIntrospectedProperty method, of class BeanUtil.
     */
    @Test
    public void testSetIntrospectedProperty() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        BeanUtil.setIntrospectedProperty(introspectedTarget, STRING_PROPERTY, NEW_VALUE);
        BeanUtil.setIntrospectedProperty(hashedTarget, STRING_PROPERTY, NEW_VALUE);
        assertThat(BeanUtil.getProperty(introspectedTarget, STRING_PROPERTY)).isEqualTo(NEW_VALUE);
        assertThat(BeanUtil.getProperty(hashedTarget, STRING_PROPERTY)).isNotEqualTo(NEW_VALUE);
        assertThat(BeanUtil.getProperty(hashedTarget, STRING_PROPERTY)).isNotEqualTo(NEW_VALUE);
    }

    /**
     * Test of getProperty method, of class BeanUtil.
     */
    @Test
    public void testGetProperty() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
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
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        assertThat(BeanUtil.getIntrospectedProperty(introspectedTarget, STRING_PROPERTY)).isEqualTo(OLD_VALUE);
        assertThat(BeanUtil.getIntrospectedProperty(introspectedTarget, NOT_A_PROPERTY)).isNull();
        assertThat(BeanUtil.getIntrospectedProperty(hashedTarget, STRING_PROPERTY)).isNull();
        assertThat(BeanUtil.getIntrospectedProperty(hashedTarget, NOT_A_PROPERTY)).isNull();
    }

    /**
     * Test of hasProperty method, of class BeanUtil.
     */
    @Test
    public void testHasProperty() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        assertThat(BeanUtil.hasProperty(new Object(), CLASS)).isTrue();
        // Object should not have this property
        assertThat(BeanUtil.hasProperty(new Object(), STRING_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasProperty(introspectedTarget, STRING_PROPERTY)).isTrue();
        assertThat(BeanUtil.hasProperty(hashedTarget, STRING_PROPERTY)).isTrue();
        assertThat(BeanUtil.hasProperty(hashedTarget, NOT_A_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasProperty(introspectedTarget, NOT_A_PROPERTY)).isFalse();
    }

    @Test
    public void testHasIndexedProperty() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        // class is not indexed
        assertThat(BeanUtil.hasIndexedProperty(new Object(), CLASS)).isFalse();
        // Object should not have this property
        assertThat(BeanUtil.hasIndexedProperty(new Object(), STRING_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIndexedProperty(introspectedTarget, STRING_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIndexedProperty(hashedTarget, STRING_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIndexedProperty(hashedTarget, NOT_A_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIndexedProperty(introspectedTarget, NOT_A_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIndexedProperty(hashedTarget, INDEXED_PROPERTY)).isTrue();
        assertThat(BeanUtil.hasIndexedProperty(introspectedTarget, INDEXED_PROPERTY)).isTrue();
    }

    /**
     * Test of hasIntrospectedProperty method, of class BeanUtil.
     */
    @Test
    public void testHasIntrospectedProperty() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        assertThat(BeanUtil.hasIntrospectedProperty(new Object(), CLASS)).isTrue();
        // Object should not have this property
        assertThat(BeanUtil.hasIntrospectedProperty(new Object(), STRING_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIntrospectedProperty(introspectedTarget, STRING_PROPERTY)).isTrue();
        // "stringProperty" is not discoverable bia introspection in hashedTarget
        assertThat(BeanUtil.hasIntrospectedProperty(hashedTarget, STRING_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIntrospectedProperty(introspectedTarget, NOT_A_PROPERTY)).isFalse();
    }

    @Test
    public void testHasIntrospectedIndexedProperty() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        // class is not indexed
        assertThat(BeanUtil.hasIntrospectedIndexedProperty(new Object(), CLASS)).isFalse();
        // Object should not have this property
        assertThat(BeanUtil.hasIntrospectedIndexedProperty(new Object(), STRING_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIntrospectedIndexedProperty(introspectedTarget, STRING_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIntrospectedIndexedProperty(hashedTarget, STRING_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIntrospectedIndexedProperty(hashedTarget, NOT_A_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIntrospectedIndexedProperty(introspectedTarget, NOT_A_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIntrospectedIndexedProperty(hashedTarget, INDEXED_PROPERTY)).isFalse();
        assertThat(BeanUtil.hasIntrospectedIndexedProperty(introspectedTarget, INDEXED_PROPERTY)).isTrue();
    }

    /**
     * Test getting properties via getPropertyNames.
     */
    @Test
    public void testGetPropertyNames() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        Set<String> expResult = new HashSet<>(2);
        Set<String> itResult = BeanUtil.getPropertyNames(introspectedTarget);
        Set<String> htResult = BeanUtil.getPropertyNames(hashedTarget);
        expResult.add(CLASS); // defined in Object
        assertThat(BeanUtil.getPropertyNames(new Object())).isEqualTo(expResult);
        expResult.add(PROPERTY_NAMES); // defined in UnboundBean
        expResult.add(STRING_PROPERTY); // defined in *Target classes in this test
        expResult.add(INDEXED_PROPERTY); // defined in *Target classes in this test
        assertThat(itResult).isEqualTo(expResult);
        assertThat(htResult).isEqualTo(expResult);
        expResult.add(NOT_A_PROPERTY);
        assertThat(htResult).isNotEqualTo(expResult);
    }

    /**
     * Test getting introspected properties via getIntrospectedPropertyNames.
     */
    @Test
    public void testGetIntrospectedPropertyNames() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        Set<String> expResult = new HashSet<>(2);
        Set<String> itResult = BeanUtil.getIntrospectedPropertyNames(introspectedTarget);
        Set<String> htResult = BeanUtil.getIntrospectedPropertyNames(hashedTarget);
        expResult.add(CLASS); // defined in Object
        assertThat(BeanUtil.getIntrospectedPropertyNames(new Object())).isEqualTo(expResult);
        expResult.add(PROPERTY_NAMES); // defined in UnboundBean
        assertThat(htResult).isEqualTo(expResult);
        expResult.add(STRING_PROPERTY); // defined in *Target classes in this test
        expResult.add(INDEXED_PROPERTY); // defined in *Target classes in this test
        assertThat(itResult).isEqualTo(expResult);
        assertThat(htResult).isNotEqualTo(expResult);
        expResult.add(NOT_A_PROPERTY);
        assertThat(itResult).isNotEqualTo(expResult);
    }

    @Test
    public void testImplementsBeanInterface() {
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
     * The following two classes define the properties "stringProperty" and
     * "indexedProperty", however ArbitraryTarget uses a HashMap to define those
     * properties, while Target uses standard JavaBeans APIs and conventions.
     */
    public class Target extends UnboundBean {

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
     * The properties "stringProperty" and "indexedProperty" are not visible in
     * the "*Introspected*" tests, but are exposed using jmri.beans.Beans
     * methods in other tests.
     */
    public class ArbitraryTarget extends UnboundArbitraryBean {

        public ArbitraryTarget() {
            this.setProperty(STRING_PROPERTY, OLD_VALUE);
            this.setIndexedProperty(INDEXED_PROPERTY, 0, OLD_VALUE);
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
