package jmri.configurexml;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Checks of java bean storage.
 *
 * @author Bob Jacobsen Copyright 2009
 */
public class DefaultJavaBeanConfigXMLTest {

    @Test
    public void testStoreBean() {
        DefaultJavaBeanConfigXML x = new DefaultJavaBeanConfigXML();
        x.store(new TestBean1());
    }

    @Test
    public void testTestBean() {
        TestBean1 tb1 = new TestBean1();
        assertTrue(tb1.equals(tb1));

        TestBean1 tb2 = new TestBean1();
        assertTrue(tb1.equals(tb2));

        tb2.setA("foo");
        tb1.setA("bar");
        assertFalse(tb1.equals(tb2));

        TestBean1 tb3 = new TestBean1();
        TestBean1 tb4 = new TestBean1();
        tb3.setB(77);
        tb4.setB(78);
        assertFalse(tb3.equals(tb4));
    }

    @Test
    public void testLoadBeanDefault() {
        DefaultJavaBeanConfigXML x = new DefaultJavaBeanConfigXML();
        TestBean1 start = new TestBean1();
        TestBean1 end = assertDoesNotThrow( () ->
            (TestBean1) x.unpack(x.store(start)));

        assertTrue(start.equals(end));
    }

    @Test
    public void testLoadBeanValue() {
        DefaultJavaBeanConfigXML x = new DefaultJavaBeanConfigXML();
        TestBean1 start = new TestBean1();
        start.setA("foo");
        start.setB(88);

        TestBean1 end = assertDoesNotThrow( () ->
            (TestBean1) x.unpack(x.store(start)));

        assertTrue(start.equals(end));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
