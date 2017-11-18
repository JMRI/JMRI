package jmri.jmrix.internal;

import org.junit.After;
import org.junit.Before;

/**
 * InternalConsistManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.internal.InternalConsistManager class
 *
 * @author	Paul Bender Copyright (C) 2017
 */
public class InternalConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
	jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugCommandStation();
        cm = new InternalConsistManager();
    }

    @After
    @Override
    public void tearDown() {
        cm = null;
        jmri.util.JUnitUtil.tearDown();
    }

}
