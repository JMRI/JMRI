package jmri.jmrix.internal;

import org.junit.jupiter.api.*;

/**
 * InternalConsistManagerTest.java
 *
 * Test for the jmri.jmrix.internal.InternalConsistManager class
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class InternalConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugCommandStation();
        cm = new InternalConsistManager();
    }

    @AfterEach
    @Override
    public void tearDown() {
        cm = null;
        jmri.util.JUnitUtil.tearDown();
    }

}
