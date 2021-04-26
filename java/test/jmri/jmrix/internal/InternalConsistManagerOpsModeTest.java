package jmri.jmrix.internal;

import org.junit.jupiter.api.*;

/**
 * InternalConsistManagerTest.java
 *
 * Test for the jmri.jmrix.internal.InternalConsistManager class
 * This set of test specifically initializes the InternalConsistManager with onl * only an ops mode Programmer available.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class InternalConsistManagerOpsModeTest extends jmri.implementation.AbstractConsistManagerTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugProgrammerManager();
        cm = new InternalConsistManager();
    }

    @AfterEach
    @Override
    public void tearDown() {
        cm = null;
        jmri.util.JUnitUtil.tearDown();
    }

}
