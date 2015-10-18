package jmri.beans;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;

/**
 *
 * @author rhwood
 */
public class UnboundArbitraryBeanTest extends TestCase {

    private static final String CLASS = "class";
    private static final String PROPERTY_NAMES = "propertyNames";
    private static final String STRING_PROPERTY = "stringProperty";
    private static final String INDEXED_PROPERTY = "indexedProperty";
    private static final String MAPPED_STRING = "mappedString";
    private static final String MAPPED_INDEXED = "mappedIndexed";
    private static final String NEW_PROPERTY = "newProperty";
    private static final String NOT_A_PROPERTY = "nonExistentProperty";
    private static final String OLD_VALUE = "old";
    private static final String NEW_VALUE = "new";

    public UnboundArbitraryBeanTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getIndexedProperty method, of class UnboundBean.
     */
    public void testGetIndexedProperty() {
        UnboundArbitraryBean instance = new UnboundBeanImpl();
        assertEquals(null, instance.getIndexedProperty(NOT_A_PROPERTY, 0));
        assertEquals(OLD_VALUE, instance.getIndexedProperty(INDEXED_PROPERTY, 0));
        // Really wish we were using JUnit 4 with its ability to assert that an
        // expected Exception was thrown
        boolean outOfBounds = false;
        try {
            instance.getIndexedProperty(INDEXED_PROPERTY, 1);
        } catch (IndexOutOfBoundsException ex) {
            outOfBounds = true;
        }
        assertTrue(outOfBounds);
        assertEquals(OLD_VALUE, instance.getIndexedProperty(MAPPED_INDEXED, 0));
        assertEquals(null, instance.getIndexedProperty(MAPPED_INDEXED, 1));
    }

    /**
     * Test of getProperty method, of class UnboundBean.
     */
    public void testGetProperty() {
        UnboundArbitraryBean instance = new UnboundBeanImpl();
        assertEquals(null, instance.getProperty(NOT_A_PROPERTY));
        assertEquals(OLD_VALUE, instance.getProperty(STRING_PROPERTY));
        assertEquals(OLD_VALUE, instance.getProperty(MAPPED_STRING));
    }

    /**
     * Test of getPropertyNames method, of class UnboundBean.
     */
    public void testGetPropertyNames() {
        UnboundArbitraryBean instance = new UnboundBeanImpl();
        Set<String> expResult = new HashSet<>(6);
        expResult.add(CLASS); // defined in Object
        expResult.add(PROPERTY_NAMES); // defined in UnboundBean
        expResult.add(STRING_PROPERTY); // defined in UnboundBeanImpl
        expResult.add(INDEXED_PROPERTY); // defined in UnboundBeanImpl
        expResult.add(MAPPED_STRING); // defined in UnboundBeanImpl
        expResult.add(MAPPED_INDEXED); // defined in UnboundBeanImpl
        Set<String> result = instance.getPropertyNames();
        assertEquals(expResult, result);
    }

    /**
     * Test of hasProperty method, of class UnboundBean.
     */
    public void testHasProperty() {
        UnboundArbitraryBean instance = new UnboundBeanImpl();
        assertTrue(instance.hasProperty(STRING_PROPERTY));
        assertTrue(instance.hasProperty(INDEXED_PROPERTY));
        assertTrue(instance.hasProperty(MAPPED_STRING));
        assertTrue(instance.hasProperty(MAPPED_INDEXED));
        assertFalse(instance.hasProperty(NOT_A_PROPERTY));
    }

    public void testHasIndexedProperty() {
        UnboundArbitraryBean instance = new UnboundBeanImpl();
        assertFalse(instance.hasIndexedProperty(STRING_PROPERTY));
        assertTrue(instance.hasIndexedProperty(INDEXED_PROPERTY));
        assertFalse(instance.hasIndexedProperty(MAPPED_STRING));
        assertTrue(instance.hasIndexedProperty(MAPPED_INDEXED));
        assertFalse(instance.hasIndexedProperty(NOT_A_PROPERTY));
    }

    /**
     * Test of setIndexedProperty method, of class UnboundBean.
     */
    public void testSetIndexedProperty() {
        UnboundArbitraryBean instance = new UnboundBeanImpl();
        instance.setIndexedProperty(INDEXED_PROPERTY, 1, NEW_VALUE);
        instance.setIndexedProperty(MAPPED_INDEXED, 1, NEW_VALUE);
        assertEquals(OLD_VALUE, instance.getIndexedProperty(INDEXED_PROPERTY, 0));
        assertEquals(OLD_VALUE, instance.getIndexedProperty(MAPPED_INDEXED, 0));
        assertEquals(NEW_VALUE, instance.getIndexedProperty(INDEXED_PROPERTY, 1));
        assertEquals(NEW_VALUE, instance.getIndexedProperty(MAPPED_INDEXED, 1));
        instance.setIndexedProperty(INDEXED_PROPERTY, 0, NEW_VALUE);
        instance.setIndexedProperty(MAPPED_INDEXED, 0, NEW_VALUE);
        assertEquals(NEW_VALUE, instance.getIndexedProperty(INDEXED_PROPERTY, 0));
        assertEquals(NEW_VALUE, instance.getIndexedProperty(MAPPED_INDEXED, 0));
        assertNull(instance.getIndexedProperty(NEW_PROPERTY, 0));
        instance.setIndexedProperty(NEW_PROPERTY, 0, NEW_VALUE);
        assertEquals(NEW_VALUE, instance.getIndexedProperty(NEW_PROPERTY, 0));
    }

    /**
     * Test of setProperty method, of class UnboundBean.
     */
    public void testSetProperty() {
        UnboundArbitraryBean instance = new UnboundBeanImpl();
        instance.setProperty(STRING_PROPERTY, NEW_VALUE);
        instance.setProperty(MAPPED_STRING, NEW_VALUE);
        assertEquals(NEW_VALUE, instance.getProperty(STRING_PROPERTY));
        assertEquals(NEW_VALUE, instance.getProperty(MAPPED_STRING));
        assertNull(instance.getProperty(NEW_PROPERTY));
        instance.setProperty(NEW_PROPERTY, OLD_VALUE);
        assertEquals(OLD_VALUE, instance.getProperty(NEW_PROPERTY));
        instance.setProperty(NEW_PROPERTY, NEW_VALUE);
        assertEquals(NEW_VALUE, instance.getProperty(NEW_PROPERTY));
    }

    public class UnboundBeanImpl extends UnboundArbitraryBean {

        private String stringProperty = OLD_VALUE;
        private final ArrayList<String> indexedProperty = new ArrayList<>();

        public UnboundBeanImpl() {
            this.indexedProperty.add(0, OLD_VALUE);
            this.setProperty(MAPPED_STRING, OLD_VALUE);
            this.setIndexedProperty(MAPPED_INDEXED, 0, OLD_VALUE);
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

}
