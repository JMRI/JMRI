package jmri.util;

import org.junit.jupiter.api.*;

/**
 * Test ReflectionUtilScaffold.
 * 
 * @author Daniel Bergqvist 2019
 */
public class ReflectionUtilScaffoldTest {

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = "SIC_INNER_SHOULD_BE_STATIC",
        justification = "testing non-static class ")
    private class MyClass {
        private String myField = "Hello World";
    }

    private static class MyStaticClass {
        private String myField = "Hello World in Static Class";
    }

    @Test
//    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public void testReflection() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        MyClass mc = new MyClass();
        Assertions.assertEquals( "Hello World", mc.myField, "Test");
        ReflectionUtilScaffold.setField(mc, "myField", "A new value");
        Assertions.assertEquals("A new value", mc.myField, "Test");
    }

    @Test
//    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public void testReflectionStatic() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        MyStaticClass mc = new MyStaticClass();
        Assertions.assertEquals( "Hello World in Static Class", mc.myField, "Private field read");
        ReflectionUtilScaffold.setField(mc, "myField", "A new static class value");
        Assertions.assertEquals( "A new static class value", mc.myField,"Private field written to");
    }

    // The minimal setup for log4J
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
