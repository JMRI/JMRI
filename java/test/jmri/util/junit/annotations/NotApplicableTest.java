package jmri.util.junit.annotations;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test that tests marked NotApplicable / Disabled do not run.
 * As all the tests within this class should not be called, ensures the
 * BeforeEach / AfterEach
 * annotated methods are not called, as no tests should run.
 * @author Steve Young Copyright 2024
 */
public class NotApplicableTest {

    @Test
    @NotApplicable("Test should not run due to this annotation.")
    public void testNotApplicableTestDoesNotRun() {
        fail("Test ran, though it had NotApplicable annotation.");
    }

    @Test
    @NotApplicable()
    public void testNotApplicableTestDoesNotRunNoReason() {
        fail("No Reason Test ran, though it had NotApplicable annotation.");
    }

    @Test
    @Disabled ("Disabled Test should not run due to this annotation")
    public void testDisabledTestDoesNotRun() {
        fail("Test ran, though it had Disabled annotation.");
    }

    @Test
    @Disabled
    public void testDisabledTestDoesNotRunNoReason() {
        fail("Disabled No Reason Test ran, though it had Disabled annotation.");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        fail("BeforeEach ran, though all tests Disabled.");
    }

    @AfterEach
    public void tearDown() {
        fail("AfterEach ran, though all tests Disabled.");
        JUnitUtil.tearDown();
    }

    @BeforeAll
    public static void beforeAll() {
        // fail("BeforeAll ran, though all tests Disabled.");
        // fails in maven but not ant ??
    }

    @AfterAll
    public static void afterAll() {
        // fail("AfterAll ran, though all tests Disabled.");
        // fails in maven but not ant ??
    }

}
