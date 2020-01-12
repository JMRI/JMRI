package jmri.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * Tests for {@link jmri.beans.Beans} static methods.
 *
 * @author Randall Wood
 */
public class BeansTest {

    private static final String CLASS = "class";
    private static final String PROPERTY_NAMES = "propertyNames";
    private static final String STRING_PROPERTY = "stringProperty";
    private static final String INDEXED_PROPERTY = "indexedProperty";
    private static final String NOT_A_PROPERTY = "nonExistentProperty";
    private static final String OLD_VALUE = "old";
    private static final String NEW_VALUE = "new";

    /**
     * Test of setIndexedProperty method, of class Beans.
     */
    @Test
    public void testSetIndexedProperty() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        Beans.setIndexedProperty(introspectedTarget, INDEXED_PROPERTY, 1, NEW_VALUE);
        Beans.setIndexedProperty(hashedTarget, INDEXED_PROPERTY, 1, NEW_VALUE);
        assertEquals(OLD_VALUE, Beans.getIndexedProperty(introspectedTarget, INDEXED_PROPERTY, 0));
        assertEquals(NEW_VALUE, Beans.getIndexedProperty(introspectedTarget, INDEXED_PROPERTY, 1));
        assertEquals(OLD_VALUE, Beans.getIndexedProperty(hashedTarget, INDEXED_PROPERTY, 0));
        assertEquals(NEW_VALUE, Beans.getIndexedProperty(hashedTarget, INDEXED_PROPERTY, 1));
    }

    /**
     * Test of setIntrospectedIndexedProperty method, of class Beans.
     */
    @Test
    public void testSetIntrospectedIndexedProperty() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        Beans.setIntrospectedIndexedProperty(introspectedTarget, INDEXED_PROPERTY, 1, NEW_VALUE);
        Beans.setIntrospectedIndexedProperty(hashedTarget, INDEXED_PROPERTY, 1, NEW_VALUE);
        assertEquals(OLD_VALUE, Beans.getIntrospectedIndexedProperty(introspectedTarget, INDEXED_PROPERTY, 0));
        assertEquals(NEW_VALUE, Beans.getIntrospectedIndexedProperty(introspectedTarget, INDEXED_PROPERTY, 1));
        assertNull(Beans.getIntrospectedIndexedProperty(hashedTarget, INDEXED_PROPERTY, 0));
        assertNull(Beans.getIntrospectedIndexedProperty(hashedTarget, INDEXED_PROPERTY, 1));
    }

    /**
     * Test of getIndexedProperty method, of class Beans.
     */
    @Test
    public void testGetIndexedProperty() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        assertEquals(OLD_VALUE, Beans.getIndexedProperty(introspectedTarget, INDEXED_PROPERTY, 0));
        assertNull(Beans.getIndexedProperty(introspectedTarget, STRING_PROPERTY, 0));
        assertNull(Beans.getIndexedProperty(introspectedTarget, NOT_A_PROPERTY, 0));
        assertEquals(OLD_VALUE, Beans.getIndexedProperty(hashedTarget, INDEXED_PROPERTY, 0));
        assertNull(Beans.getIndexedProperty(hashedTarget, STRING_PROPERTY, 0));
        assertNull(Beans.getIndexedProperty(hashedTarget, NOT_A_PROPERTY, 0));
    }

    /**
     * Test of getIntrospectedIndexedProperty method, of class Beans.
     */
    @Test
    public void testGetIntrospectedIndexedProperty() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        assertEquals(OLD_VALUE, Beans.getIntrospectedIndexedProperty(introspectedTarget, INDEXED_PROPERTY, 0));
        assertNull(Beans.getIntrospectedIndexedProperty(introspectedTarget, STRING_PROPERTY, 0));
        assertNull(Beans.getIntrospectedIndexedProperty(introspectedTarget, NOT_A_PROPERTY, 0));
        assertNull(Beans.getIntrospectedIndexedProperty(hashedTarget, INDEXED_PROPERTY, 0));
        assertNull(Beans.getIntrospectedIndexedProperty(hashedTarget, STRING_PROPERTY, 0));
        assertNull(Beans.getIntrospectedIndexedProperty(hashedTarget, NOT_A_PROPERTY, 0));
    }

    /**
     * Test of setProperty method, of class Beans.
     */
    @Test
    public void testSetProperty() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        Beans.setProperty(introspectedTarget, STRING_PROPERTY, NEW_VALUE);
        Beans.setProperty(hashedTarget, STRING_PROPERTY, NEW_VALUE);
        assertEquals(NEW_VALUE, Beans.getProperty(introspectedTarget, STRING_PROPERTY));
        assertEquals(NEW_VALUE, Beans.getProperty(hashedTarget, STRING_PROPERTY));
    }

    /**
     * Test of setIntrospectedProperty method, of class Beans.
     */
    @Test
    public void testSetIntrospectedProperty() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        Beans.setIntrospectedProperty(introspectedTarget, STRING_PROPERTY, NEW_VALUE);
        Beans.setIntrospectedProperty(hashedTarget, STRING_PROPERTY, NEW_VALUE);
        assertEquals(NEW_VALUE, Beans.getProperty(introspectedTarget, STRING_PROPERTY));
        assertNotSame(NEW_VALUE, Beans.getProperty(hashedTarget, STRING_PROPERTY));
    }

    /**
     * Test of getProperty method, of class Beans.
     */
    @Test
    public void testGetProperty() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        assertEquals(OLD_VALUE, Beans.getProperty(introspectedTarget, STRING_PROPERTY));
        assertNull(Beans.getProperty(introspectedTarget, NOT_A_PROPERTY));
        assertEquals(OLD_VALUE, Beans.getProperty(hashedTarget, STRING_PROPERTY));
        assertNull(Beans.getProperty(hashedTarget, NOT_A_PROPERTY));
    }

    /**
     * Test of getIntrospectedProperty method, of class Beans.
     */
    @Test
    public void testGetIntrospectedProperty() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        assertEquals(OLD_VALUE, Beans.getIntrospectedProperty(introspectedTarget, STRING_PROPERTY));
        assertNull(Beans.getIntrospectedProperty(introspectedTarget, NOT_A_PROPERTY));
        assertNull(Beans.getIntrospectedProperty(hashedTarget, STRING_PROPERTY));
        assertNull(Beans.getIntrospectedProperty(hashedTarget, NOT_A_PROPERTY));
    }

    /**
     * Test of hasProperty method, of class Beans.
     */
    @Test
    public void testHasProperty() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        assertTrue(Beans.hasProperty(new Object(), CLASS));
        // Object should not have this property
        assertFalse(Beans.hasProperty(new Object(), STRING_PROPERTY));
        assertTrue(Beans.hasProperty(introspectedTarget, STRING_PROPERTY));
        assertTrue(Beans.hasProperty(hashedTarget, STRING_PROPERTY));
        assertFalse(Beans.hasProperty(hashedTarget, NOT_A_PROPERTY));
        assertFalse(Beans.hasProperty(introspectedTarget, NOT_A_PROPERTY));
    }

    @Test
    public void testHasIndexedProperty() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        // class is not indexed
        assertFalse(Beans.hasIndexedProperty(new Object(), CLASS));
        // Object should not have this property
        assertFalse(Beans.hasIndexedProperty(new Object(), STRING_PROPERTY));
        assertFalse(Beans.hasIndexedProperty(introspectedTarget, STRING_PROPERTY));
        assertFalse(Beans.hasIndexedProperty(hashedTarget, STRING_PROPERTY));
        assertFalse(Beans.hasIndexedProperty(hashedTarget, NOT_A_PROPERTY));
        assertFalse(Beans.hasIndexedProperty(introspectedTarget, NOT_A_PROPERTY));
        assertTrue(Beans.hasIndexedProperty(hashedTarget, INDEXED_PROPERTY));
        assertTrue(Beans.hasIndexedProperty(introspectedTarget, INDEXED_PROPERTY));
    }

    /**
     * Test of hasIntrospectedProperty method, of class Beans.
     */
    @Test
    public void testHasIntrospectedProperty() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        assertTrue(Beans.hasIntrospectedProperty(new Object(), CLASS));
        // Object should not have this property
        assertFalse(Beans.hasIntrospectedProperty(new Object(), STRING_PROPERTY));
        assertTrue(Beans.hasIntrospectedProperty(introspectedTarget, STRING_PROPERTY));
        // "stringProperty" is not discoverable bia introspection in hashedTarget
        assertFalse(Beans.hasIntrospectedProperty(hashedTarget, STRING_PROPERTY));
        assertFalse(Beans.hasIntrospectedProperty(introspectedTarget, NOT_A_PROPERTY));
    }

    @Test
    public void testHasIntrospectedIndexedProperty() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        // class is not indexed
        assertFalse(Beans.hasIntrospectedIndexedProperty(new Object(), CLASS));
        // Object should not have this property
        assertFalse(Beans.hasIntrospectedIndexedProperty(new Object(), STRING_PROPERTY));
        assertFalse(Beans.hasIntrospectedIndexedProperty(introspectedTarget, STRING_PROPERTY));
        assertFalse(Beans.hasIntrospectedIndexedProperty(hashedTarget, STRING_PROPERTY));
        assertFalse(Beans.hasIntrospectedIndexedProperty(hashedTarget, NOT_A_PROPERTY));
        assertFalse(Beans.hasIntrospectedIndexedProperty(introspectedTarget, NOT_A_PROPERTY));
        assertFalse(Beans.hasIntrospectedIndexedProperty(hashedTarget, INDEXED_PROPERTY));
        assertTrue(Beans.hasIntrospectedIndexedProperty(introspectedTarget, INDEXED_PROPERTY));
    }

    /**
     * Test getting properties via getPropertyNames.
     */
    @Test
    public void testGetPropertyNames() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        Set<String> expResult = new HashSet<>(2);
        Set<String> itResult = Beans.getPropertyNames(introspectedTarget);
        Set<String> htResult = Beans.getPropertyNames(hashedTarget);
        expResult.add(CLASS); // defined in Object
        assertEquals(expResult, Beans.getPropertyNames(new Object()));
        expResult.add(PROPERTY_NAMES); // defined in UnboundBean
        expResult.add(STRING_PROPERTY); // defined in *Target classes in this test
        expResult.add(INDEXED_PROPERTY); // defined in *Target classes in this test
        assertEquals(expResult, itResult);
        assertEquals(expResult, htResult);
        expResult.add(NOT_A_PROPERTY);
        assertNotSame(expResult, htResult);
    }

    /**
     * Test getting introspected properties via getIntrospectedPropertyNames.
     */
    @Test
    public void testGetIntrospectedPropertyNames() {
        Object introspectedTarget = new Target();
        Object hashedTarget = new ArbitraryTarget();
        Set<String> expResult = new HashSet<>(2);
        Set<String> itResult = Beans.getIntrospectedPropertyNames(introspectedTarget);
        Set<String> htResult = Beans.getIntrospectedPropertyNames(hashedTarget);
        expResult.add(CLASS); // defined in Object
        assertEquals(expResult, Beans.getIntrospectedPropertyNames(new Object()));
        expResult.add(PROPERTY_NAMES); // defined in UnboundBean
        assertEquals(expResult, htResult);
        expResult.add(STRING_PROPERTY); // defined in *Target classes in this test
        expResult.add(INDEXED_PROPERTY); // defined in *Target classes in this test
        assertEquals(expResult, itResult);
        assertNotSame(expResult, htResult);
        expResult.add(NOT_A_PROPERTY);
        assertNotSame(expResult, itResult);
    }

    @Test
    public void testImplementsBeanInterface() {
        assertEquals(false, Beans.implementsBeanInterface(null));
        assertEquals(false, Beans.implementsBeanInterface(new Object()));
        assertEquals(true, Beans.implementsBeanInterface(new Bean() {
        }));
    }

    @Test
    public void testContains() {
        Listener l1 = new Listener();
        Listener l2 = new Listener();
        Listener l3 = new Listener();
        PropertyChangeListener[] listeners = {l1, l2};
        assertTrue(Beans.contains(listeners, l1));
        assertTrue(Beans.contains(listeners, l2));
        assertFalse(Beans.contains(listeners, l3));
        listeners[1] = new PropertyChangeListenerProxy("foo", l2);
        assertTrue(Beans.contains(listeners, l1));
        assertTrue(Beans.contains(listeners, l2));
        assertFalse(Beans.contains(listeners, l3));
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
