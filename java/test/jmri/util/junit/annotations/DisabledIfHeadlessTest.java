package jmri.util.junit.annotations;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test DisabledIfHeadless annotation applied to a test method.
 * @author Steve Young Copyright 2024
 */
public class DisabledIfHeadlessTest {

    private static boolean isHeadless() {
        return java.awt.GraphicsEnvironment.isHeadless();
    }

    private static int methodCalls = 0;

    @Test
    @DisabledIfHeadless
    public void testNotApplicableTestDoesNotRun() {
        if ( isHeadless() ) {
            fail("Test ran, though has DisabledIfHeadless annotation.");
        } else {
            methodCalls++;
        }
    }

    @BeforeEach
    public void setUp() {
        if ( isHeadless() ) {
            fail("BeforeEach ran, though test Disabled as in headless mode.");
        }
        methodCalls++;
    }

    @AfterEach
    public void tearDown() {
        if ( isHeadless() ) {
            fail("AfterEach ran, though test Disabled as in headless mode.");
        }
        methodCalls++;
    }

    @BeforeAll
    public static void beforeAll() {
        JUnitUtil.setUp();
        methodCalls = 0;
    }

    @AfterAll
    public static void afterAll() {
        if ( ! isHeadless() ) {
            Assertions.assertEquals(3, methodCalls, "headed test ran beforeEach, Test, AfterEach");
        }
        JUnitUtil.tearDown();
    }

}
